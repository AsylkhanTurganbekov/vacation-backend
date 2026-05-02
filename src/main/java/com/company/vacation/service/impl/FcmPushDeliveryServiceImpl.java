package com.company.vacation.service.impl;

import com.company.vacation.dto.notification.PushSendResult;
import com.company.vacation.dto.notification.PushTokenResult;
import com.company.vacation.service.DevicePushTokenService;
import com.company.vacation.service.PushDeliveryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushDeliveryServiceImpl implements PushDeliveryService {

    private static final String FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final long ACCESS_TOKEN_REFRESH_SKEW_SECONDS = 60L;

    private final DevicePushTokenService devicePushTokenService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:}}")
    private String serviceAccountPath;

    @Value("${app.firebase.service-account-base64:${FIREBASE_SERVICE_ACCOUNT_BASE64:}}")
    private String serviceAccountBase64;

    private volatile ServiceAccountConfig serviceAccountConfig;
    private volatile OAuthAccessToken cachedAccessToken;
    private volatile String firebaseProjectId;
    private volatile String lastConfigurationReason = "firebase_not_initialized";

    @Override
    public PushSendResult sendToTokens(Collection<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.builder()
                    .configured(isConfigured())
                    .reason(isConfigured() ? "no_tokens" : currentConfigurationReason())
                    .projectId(firebaseProjectId)
                    .requestedTokens(0)
                    .successCount(0)
                    .failureCount(0)
                    .tokenResults(List.of())
                    .build();
        }

        ServiceAccountConfig config = loadServiceAccountConfig();
        if (config == null) {
            log.warn("FCM is not configured; skipping push delivery for {} tokens", tokens.size());
            return buildUnconfiguredResult(tokens, currentConfigurationReason(), "Push provider is not initialized");
        }

        OAuthAccessToken accessToken;
        try {
            accessToken = accessToken(config);
        } catch (Exception exception) {
            lastConfigurationReason = "oauth_token_refresh_failed";
            log.error("Failed to obtain Firebase OAuth access token: {}", exception.getMessage(), exception);
            return buildUnconfiguredResult(tokens, lastConfigurationReason, exception.getMessage());
        }

        List<PushTokenResult> tokenResults = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        for (String token : tokens) {
            PushTokenResult result = sendSingleToken(config, accessToken.tokenValue(), token, title, body, data);
            tokenResults.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        return PushSendResult.builder()
                .configured(true)
                .reason("ok")
                .projectId(firebaseProjectId)
                .requestedTokens(tokens.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .tokenResults(tokenResults)
                .build();
    }

    private PushTokenResult sendSingleToken(
            ServiceAccountConfig config,
            String accessToken,
            String token,
            String title,
            String body,
            Map<String, String> data
    ) {
        try {
            String endpoint = "https://fcm.googleapis.com/v1/projects/" + config.projectId() + "/messages:send";
            String payload = objectMapper.writeValueAsString(Map.of(
                    "message", Map.of(
                            "token", token,
                            "notification", Map.of("title", title, "body", body),
                            "data", data == null ? Map.of() : data
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("FCM token delivery succeeded: token={}", maskToken(token));
                return PushTokenResult.builder()
                        .tokenMasked(maskToken(token))
                        .success(true)
                        .build();
            }

            String errorCode = extractErrorCode(response.body());
            String errorMessage = extractErrorMessage(response.body());
            log.warn(
                    "FCM token delivery failed: token={}, status={}, code={}, message={}",
                    maskToken(token),
                    response.statusCode(),
                    errorCode,
                    errorMessage
            );
            if ("unregistered".equals(errorCode)
                    || "invalid_argument".equals(errorCode)
                    || "sender_id_mismatch".equals(errorCode)) {
                devicePushTokenService.deactivateInvalidToken(token);
            }
            return PushTokenResult.builder()
                    .tokenMasked(maskToken(token))
                    .success(false)
                    .errorCode(errorCode)
                    .errorMessage(errorMessage)
                    .build();
        } catch (Exception exception) {
            log.error("FCM token delivery failed: token={}, message={}", maskToken(token), exception.getMessage(), exception);
            return PushTokenResult.builder()
                    .tokenMasked(maskToken(token))
                    .success(false)
                    .errorCode("token_send_failed")
                    .errorMessage(exception.getMessage())
                    .build();
        }
    }

    private PushSendResult buildUnconfiguredResult(Collection<String> tokens, String reason, String errorMessage) {
        return PushSendResult.builder()
                .configured(false)
                .reason(reason)
                .projectId(firebaseProjectId)
                .requestedTokens(tokens.size())
                .successCount(0)
                .failureCount(tokens.size())
                .tokenResults(tokens.stream()
                        .map(token -> PushTokenResult.builder()
                                .tokenMasked(maskToken(token))
                                .success(false)
                                .errorCode(reason)
                                .errorMessage(errorMessage)
                                .build())
                        .toList())
                .build();
    }

    private OAuthAccessToken accessToken(ServiceAccountConfig config) throws Exception {
        OAuthAccessToken current = cachedAccessToken;
        Instant now = Instant.now();
        if (current != null && current.expiresAt().isAfter(now.plusSeconds(ACCESS_TOKEN_REFRESH_SKEW_SECONDS))) {
            lastConfigurationReason = "ok";
            return current;
        }

        synchronized (this) {
            current = cachedAccessToken;
            now = Instant.now();
            if (current != null && current.expiresAt().isAfter(now.plusSeconds(ACCESS_TOKEN_REFRESH_SKEW_SECONDS))) {
                lastConfigurationReason = "ok";
                return current;
            }

            String jwtAssertion = Jwts.builder()
                    .issuer(config.clientEmail())
                    .claim("scope", FIREBASE_MESSAGING_SCOPE)
                    .claim("aud", config.tokenUri())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(3600)))
                    .signWith(config.privateKey(), Jwts.SIG.RS256)
                    .compact();

            String form = "grant_type=" + urlEncode("urn:ietf:params:oauth:grant-type:jwt-bearer")
                    + "&assertion=" + urlEncode(jwtAssertion);

            HttpRequest request = HttpRequest.newBuilder(URI.create(config.tokenUri()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("OAuth token exchange failed: HTTP " + response.statusCode() + " -> " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode tokenNode = json.get("access_token");
            JsonNode expiresInNode = json.get("expires_in");
            if (tokenNode == null || tokenNode.isNull()) {
                throw new IOException("OAuth token exchange returned no access_token");
            }

            long expiresIn = expiresInNode != null && expiresInNode.canConvertToLong() ? expiresInNode.asLong() : 3600L;
            OAuthAccessToken refreshed = new OAuthAccessToken(tokenNode.asText(), now.plusSeconds(expiresIn));
            cachedAccessToken = refreshed;
            lastConfigurationReason = "ok";
            return refreshed;
        }
    }

    private ServiceAccountConfig loadServiceAccountConfig() {
        ServiceAccountConfig current = serviceAccountConfig;
        if (current != null) {
            lastConfigurationReason = "ok";
            return current;
        }

        synchronized (this) {
            if (serviceAccountConfig != null) {
                lastConfigurationReason = "ok";
                return serviceAccountConfig;
            }
            try {
                String credentialsJson = credentialsJson();
                if (credentialsJson == null) {
                    lastConfigurationReason = "missing_service_account";
                    return null;
                }

                JsonNode root = objectMapper.readTree(credentialsJson);
                String projectId = text(root, "project_id");
                String clientEmail = text(root, "client_email");
                String privateKeyPem = text(root, "private_key");
                String tokenUri = text(root, "token_uri");
                if (tokenUri == null || tokenUri.isBlank()) {
                    tokenUri = DEFAULT_TOKEN_URI;
                }
                if (projectId == null || projectId.isBlank()) {
                    lastConfigurationReason = "missing_project_id";
                    return null;
                }
                if (clientEmail == null || clientEmail.isBlank() || privateKeyPem == null || privateKeyPem.isBlank()) {
                    lastConfigurationReason = "invalid_service_account";
                    return null;
                }

                firebaseProjectId = projectId;
                serviceAccountConfig = new ServiceAccountConfig(
                        projectId,
                        clientEmail,
                        tokenUri,
                        parsePrivateKey(privateKeyPem)
                );
                lastConfigurationReason = "ok";
                return serviceAccountConfig;
            } catch (NoSuchFileException exception) {
                lastConfigurationReason = "missing_service_account_file";
                log.error("Failed to load Firebase service account: {}", exception.getMessage(), exception);
                return null;
            } catch (Exception exception) {
                lastConfigurationReason = "firebase_not_initialized";
                log.error("Failed to load Firebase service account: {}", exception.getMessage(), exception);
                return null;
            }
        }
    }

    private boolean isConfigured() {
        return (serviceAccountPath != null && !serviceAccountPath.isBlank())
                || (serviceAccountBase64 != null && !serviceAccountBase64.isBlank());
    }

    private String currentConfigurationReason() {
        if (!isConfigured()) {
            return "missing_service_account";
        }
        return lastConfigurationReason;
    }

    private String credentialsJson() throws IOException {
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return Files.readString(Path.of(serviceAccountPath), StandardCharsets.UTF_8);
        }
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
            return new String(decoded, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws InvalidKeySpecException {
        try {
            String normalized = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Decoders.BASE64.decode(normalized);
            PrivateKey privateKey = Keys.keyPairFor(io.jsonwebtoken.SignatureAlgorithm.RS256).getPrivate();
            return (RSAPrivateKey) java.security.KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (InvalidKeySpecException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidKeySpecException("Failed to parse Firebase private key", exception);
        }
    }

    private String extractErrorCode(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode details = root.path("error").path("details");
            if (details.isArray()) {
                for (JsonNode detail : details) {
                    JsonNode code = detail.get("errorCode");
                    if (code != null && !code.isNull()) {
                        return code.asText().toLowerCase();
                    }
                }
            }
            JsonNode status = root.path("error").path("status");
            if (!status.isMissingNode() && !status.isNull()) {
                return status.asText().toLowerCase();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "token_send_failed";
    }

    private String extractErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode message = root.path("error").path("message");
            if (!message.isMissingNode() && !message.isNull()) {
                return message.asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return body;
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        if (token.length() <= 12) {
            return token.substring(0, Math.min(4, token.length())) + "***";
        }
        return token.substring(0, 6) + "***" + token.substring(token.length() - 6);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record ServiceAccountConfig(
            String projectId,
            String clientEmail,
            String tokenUri,
            RSAPrivateKey privateKey
    ) {
    }

    private record OAuthAccessToken(String tokenValue, Instant expiresAt) {
    }
}
