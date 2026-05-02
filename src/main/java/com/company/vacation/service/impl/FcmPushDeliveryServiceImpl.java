package com.company.vacation.service.impl;

import com.company.vacation.dto.notification.PushSendResult;
import com.company.vacation.service.DevicePushTokenService;
import com.company.vacation.service.PushDeliveryService;
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

    @Value("${app.firebase.service-account-path:${FIREBASE_SERVICE_ACCOUNT_PATH:}}")
    private String serviceAccountPath;

    @Value("${app.firebase.service-account-base64:${FIREBASE_SERVICE_ACCOUNT_BASE64:}}")
    private String serviceAccountBase64;

    private volatile FirebaseMessaging firebaseMessaging;
    private static final String FIREBASE_APP_NAME = "triply-fcm";

    @Override
    public PushSendResult sendToTokens(Collection<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return PushSendResult.builder()
                    .configured(isConfigured())
                    .requestedTokens(0)
                    .successCount(0)
                    .failureCount(0)
                    .build();
        }

        FirebaseMessaging messaging = firebaseMessaging();
        if (messaging == null) {
            log.warn("FCM is not configured; skipping push delivery for {} tokens", tokens.size());
            return PushSendResult.builder()
                    .configured(false)
                    .requestedTokens(tokens.size())
                    .successCount(0)
                    .failureCount(tokens.size())
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
            for (int index = 0; index < response.getResponses().size(); index++) {
                SendResponse sendResponse = response.getResponses().get(index);
                if (!sendResponse.isSuccessful() && sendResponse.getException() != null) {
                    handleSendException(tokenList.get(index), sendResponse.getException());
                }
            }
            return PushSendResult.builder()
                    .configured(true)
                    .requestedTokens(tokenList.size())
                    .successCount(response.getSuccessCount())
                    .failureCount(response.getFailureCount())
                    .build();
        } catch (FirebaseMessagingException exception) {
            log.error("FCM batch send failed: {}", exception.getMessage(), exception);
            return PushSendResult.builder()
                    .configured(true)
                    .requestedTokens(tokenList.size())
                    .successCount(0)
                    .failureCount(tokenList.size())
                    .build();
        }
    }

    private boolean isConfigured() {
        return (serviceAccountPath != null && !serviceAccountPath.isBlank())
                || (serviceAccountBase64 != null && !serviceAccountBase64.isBlank());
    }

    private FirebaseMessaging firebaseMessaging() {
        FirebaseMessaging current = firebaseMessaging;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (firebaseMessaging != null) {
                return firebaseMessaging;
            }
            try (InputStream inputStream = openCredentialsStream()) {
                if (inputStream == null) {
                    return null;
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
                FirebaseApp app = FirebaseApp.getApps().stream()
                        .filter(existing -> FIREBASE_APP_NAME.equals(existing.getName()))
                        .findFirst()
                        .orElseGet(() -> FirebaseApp.initializeApp(options, FIREBASE_APP_NAME));
                firebaseMessaging = FirebaseMessaging.getInstance(app);
                return firebaseMessaging;
            } catch (Exception exception) {
                log.error("Failed to initialize Firebase Messaging: {}", exception.getMessage(), exception);
                return null;
            }
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            return Files.newInputStream(Path.of(serviceAccountPath));
        }
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64);
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
}
