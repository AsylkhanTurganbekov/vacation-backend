package com.company.vacation.service.impl;

import com.company.vacation.exception.BusinessException;
import com.company.vacation.service.BiometricProvider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Production adapter for an external face-verification service. It deliberately
 * does not log image payloads and fails closed when the provider is unavailable.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.biometric.provider", havingValue = "http")
public class HttpBiometricProvider implements BiometricProvider {

    private final ObjectMapper objectMapper;

    @Value("${app.biometric.http.base-url:}")
    private String baseUrl;

    @Value("${app.biometric.http.api-key:}")
    private String apiKey;

    @Value("${app.biometric.http.connect-timeout-ms:2000}")
    private long connectTimeoutMs;

    @Value("${app.biometric.http.request-timeout-ms:8000}")
    private long requestTimeoutMs;

    @Override
    public VerificationResult verify(Long employeeId, String imagePayload) {
        if (baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("Biometric HTTP provider is not configured");
        }
        try {
            String requestBody = objectMapper.writeValueAsString(new VerificationRequest(employeeId, imagePayload, UUID.randomUUID().toString()));
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/face-verifications"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(requestTimeoutMs))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                    .build();
            HttpResponse<String> response = sendWithSingleRetry(client, request);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("Biometric provider rejected the verification request");
            }
            VerificationResponse body = objectMapper.readValue(response.body(), VerificationResponse.class);
            if (body.verified == null || body.score == null) {
                throw new BusinessException("Biometric provider returned an invalid response");
            }
            return new VerificationResult(body.verified, body.score,
                    body.provider == null || body.provider.isBlank() ? "external-biometric-provider" : body.provider);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("Biometric provider is unavailable");
        }
    }

    private HttpResponse<String> sendWithSingleRetry(HttpClient client, HttpRequest request) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 500 || attempt == 1) {
                    return response;
                }
            } catch (java.io.IOException exception) {
                lastException = exception;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw exception;
            }
        }
        throw lastException == null ? new java.io.IOException("Biometric provider did not respond") : lastException;
    }

    private record VerificationRequest(Long employeeId, String imageBase64, String correlationId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class VerificationResponse {
        public Boolean verified;
        public BigDecimal score;
        public String provider;
    }
}
