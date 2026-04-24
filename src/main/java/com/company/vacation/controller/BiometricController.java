package com.company.vacation.controller;

import com.company.vacation.dto.biometric.BiometricVerificationResponse;
import com.company.vacation.dto.biometric.BiometricVerifyRequest;
import com.company.vacation.service.BiometricVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/biometric")
@RequiredArgsConstructor
public class BiometricController {

    private final BiometricVerificationService biometricVerificationService;

    @PostMapping("/verify")
    public BiometricVerificationResponse verify(@Valid @RequestBody BiometricVerifyRequest request) {
        return biometricVerificationService.verify(request);
    }

    @GetMapping("/verifications/{id}")
    public BiometricVerificationResponse getVerification(@PathVariable Long id) {
        return biometricVerificationService.getById(id);
    }
}
