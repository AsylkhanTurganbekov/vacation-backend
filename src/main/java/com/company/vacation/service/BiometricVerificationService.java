package com.company.vacation.service;

import com.company.vacation.dto.biometric.BiometricVerificationResponse;
import com.company.vacation.dto.biometric.BiometricVerifyRequest;

public interface BiometricVerificationService {

    BiometricVerificationResponse verify(BiometricVerifyRequest request);

    BiometricVerificationResponse getById(Long id);
}
