package com.company.vacation.service;

import java.math.BigDecimal;

public interface BiometricProvider {

    VerificationResult verify(Long employeeId, String imagePayload);

    record VerificationResult(boolean verified, BigDecimal score, String providerName) {
    }
}
