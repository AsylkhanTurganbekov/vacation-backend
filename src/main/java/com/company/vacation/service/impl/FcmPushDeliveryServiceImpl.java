package com.company.vacation.service.impl;

import com.company.vacation.dto.notification.PushSendResult;
import com.company.vacation.dto.notification.PushTokenResult;
import com.company.vacation.service.DevicePushTokenService;
import com.company.vacation.service.PushDeliveryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
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

    private final DevicePushTokenService devicePushTokenService;
    private final ObjectMapper objectMapper;

    @Value("${app.firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:}}")
    private String serviceAccountPath;

    @Value("${app.firebase.service-account-base64:${FIREBASE_SERVICE_ACCOUNT_BASE64:}}")
    private String serviceAccountBase64;

    private volatile FirebaseMessaging firebaseMessaging;
    private volatile String firebaseProjectId;
    private volatile String lastConfigurationReason = "firebase_not_initialized";
    private static final String FIREBASE_APP_NAME = "triply-fcm";
    private static final String FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

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

        FirebaseMessaging messaging = firebaseMessaging();
        if (messaging == null) {
            log.warn("FCM is not configured; skipping push delivery for {} tokens", tokens.size());
            return PushSendResult.builder()
                    .configured(false)
                    .reason(currentConfigurationReason())
                    .projectId(firebaseProjectId)
                    .requestedTokens(tokens.size())
                    .successCount(0)
                    .failureCount(tokens.size())
                    .tokenResults(tokens.stream()
                            .map(token -> PushTokenResult.builder()
                                    .tokenMasked(maskToken(token))
                                    .success(false)
                                    .errorCode(currentConfigurationReason())
                                    .errorMessage("Push provider is not initialized")
                                    .build())
                            .toList())
                    .build();
        }

        List<String> tokenList = new ArrayList<>(tokens);
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .addAllTokens(tokenList)
                .build();

        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            log.info("FCM send completed: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
            List<PushTokenResult> tokenResults = new ArrayList<>();
            for (int index = 0; index < response.getResponses().size(); index++) {
                SendResponse sendResponse = response.getResponses().get(index);
                String token = tokenList.get(index);
                if (sendResponse.isSuccessful()) {
                    tokenResults.add(PushTokenResult.builder()
                            .tokenMasked(maskToken(token))
                            .success(true)
                            .build());
                } else if (sendResponse.getException() != null) {
                    FirebaseMessagingException exception = sendResponse.getException();
                    handleSendException(token, exception);
                    tokenResults.add(PushTokenResult.builder()
                            .tokenMasked(maskToken(token))
                            .success(false)
                            .errorCode(exception.getMessagingErrorCode() != null
                                    ? exception.getMessagingErrorCode().name().toLowerCase()
                                    : "token_send_failed")
                            .errorMessage(exception.getMessage())
                            .build());
                } else {
                    tokenResults.add(PushTokenResult.builder()
                            .tokenMasked(maskToken(token))
                            .success(false)
                            .errorCode("token_send_failed")
                            .errorMessage("Unknown FCM send failure")
                            .build());
                }
            }
            return PushSendResult.builder()
                    .configured(true)
                    .reason("ok")
                    .projectId(firebaseProjectId)
                    .requestedTokens(tokenList.size())
                    .successCount(response.getSuccessCount())
                    .failureCount(response.getFailureCount())
                    .tokenResults(tokenResults)
                    .build();
        } catch (FirebaseMessagingException exception) {
            log.error("FCM batch send failed: {}", exception.getMessage(), exception);
            return PushSendResult.builder()
                    .configured(true)
                    .reason("token_send_failed")
                    .projectId(firebaseProjectId)
                    .requestedTokens(tokenList.size())
                    .successCount(0)
                    .failureCount(tokenList.size())
                    .tokenResults(tokenList.stream()
                            .map(token -> PushTokenResult.builder()
                                    .tokenMasked(maskToken(token))
                                    .success(false)
                                    .errorCode(exception.getMessagingErrorCode() != null
                                            ? exception.getMessagingErrorCode().name().toLowerCase()
                                            : "token_send_failed")
                                    .errorMessage(exception.getMessage())
                                    .build())
                            .toList())
                    .build();
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

    private FirebaseMessaging firebaseMessaging() {
        FirebaseMessaging current = firebaseMessaging;
        if (current != null) {
            lastConfigurationReason = "ok";
            return current;
        }

        synchronized (this) {
            if (firebaseMessaging != null) {
                lastConfigurationReason = "ok";
                return firebaseMessaging;
            }
            try (InputStream inputStream = openCredentialsStream()) {
                if (inputStream == null) {
                    lastConfigurationReason = "missing_service_account";
                    return null;
                }
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
                if (credentials.createScopedRequired()) {
                    credentials = credentials.createScoped(List.of(FIREBASE_MESSAGING_SCOPE));
                }
                try {
                    credentials.refreshIfExpired();
                } catch (IOException exception) {
                    throw new IOException(buildRefreshErrorMessage(exception), exception);
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(firebaseProjectId)
                        .build();
                FirebaseApp app = FirebaseApp.getApps().stream()
                        .filter(existing -> FIREBASE_APP_NAME.equals(existing.getName()))
                        .findFirst()
                        .orElseGet(() -> FirebaseApp.initializeApp(options, FIREBASE_APP_NAME));
                firebaseMessaging = FirebaseMessaging.getInstance(app);
                firebaseProjectId = app.getOptions().getProjectId();
                lastConfigurationReason = "ok";
                return firebaseMessaging;
            } catch (NoSuchFileException exception) {
                lastConfigurationReason = "missing_service_account_file";
                log.error("Failed to initialize Firebase Messaging: {}", exception.getMessage(), exception);
                return null;
            } catch (Exception exception) {
                lastConfigurationReason = "firebase_not_initialized";
                log.error("Failed to initialize Firebase Messaging: {}", exception.getMessage(), exception);
                Throwable cause = exception.getCause();
                while (cause != null) {
                    log.error("Firebase init cause: {}", cause.getMessage(), cause);
                    cause = cause.getCause();
                }
                return null;
            }
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            firebaseProjectId = parseProjectId(Files.readString(Path.of(serviceAccountPath), StandardCharsets.UTF_8));
            return Files.newInputStream(Path.of(serviceAccountPath));
        }
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
            firebaseProjectId = parseProjectId(new String(decoded, StandardCharsets.UTF_8));
            return new ByteArrayInputStream(decoded);
        }
        return null;
    }

    private void handleSendException(String token, FirebaseMessagingException exception) {
        MessagingErrorCode code = exception.getMessagingErrorCode();
        log.warn("FCM token delivery failed: token={}, code={}, message={}", token, code, exception.getMessage());
        if (code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT
                || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
            devicePushTokenService.deactivateInvalidToken(token);
        }
    }

    private String parseProjectId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode projectId = node.get("project_id");
            return projectId != null && !projectId.isNull() ? projectId.asText() : null;
        } catch (Exception exception) {
            return null;
        }
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

    private String buildRefreshErrorMessage(IOException exception) {
        StringBuilder message = new StringBuilder("Unexpected error refreshing access token");
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
                message.append(" -> ").append(cause.getClass().getSimpleName()).append(": ").append(cause.getMessage());
            } else {
                message.append(" -> ").append(cause.getClass().getSimpleName());
            }
            cause = cause.getCause();
        }
        return message.toString();
    }
}
