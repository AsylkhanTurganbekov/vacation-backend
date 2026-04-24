package com.company.vacation.service.impl;

import com.company.vacation.service.BiometricProvider;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MockBiometricProvider implements BiometricProvider {

    private final String providerName;

    public MockBiometricProvider(@Value("${app.biometric.mock-provider-name}") String providerName) {
        this.providerName = providerName;
    }

    @Override
    public VerificationResult verify(Long employeeId, String imagePayload) {
        boolean verified = imagePayload != null && !imagePayload.isBlank();
        BigDecimal score = verified ? BigDecimal.valueOf(0.95) : BigDecimal.ZERO;
        return new VerificationResult(verified, score, providerName);
    }
}
