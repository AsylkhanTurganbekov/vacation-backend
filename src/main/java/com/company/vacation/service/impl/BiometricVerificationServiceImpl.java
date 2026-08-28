package com.company.vacation.service.impl;

import com.company.vacation.dto.biometric.BiometricVerificationResponse;
import com.company.vacation.dto.biometric.BiometricVerifyRequest;
import com.company.vacation.entity.BiometricVerification;
import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.entity.enums.VerificationStatus;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.mapper.BiometricVerificationMapper;
import com.company.vacation.repository.BiometricVerificationRepository;
import com.company.vacation.repository.TripEventRepository;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.BiometricProvider;
import com.company.vacation.service.BiometricVerificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class BiometricVerificationServiceImpl implements BiometricVerificationService {

    private final BiometricVerificationRepository biometricVerificationRepository;
    private final TripEventRepository tripEventRepository;
    private final UserRepository userRepository;
    private final BiometricProvider biometricProvider;
    private final BiometricVerificationMapper biometricVerificationMapper;
    private final AuditLogService auditLogService;
    private final AuthContextService authContextService;

    @Override
    @Transactional
    public BiometricVerificationResponse verify(BiometricVerifyRequest request) {
        TripEvent tripEvent = tripEventRepository.findById(request.getTripEventId())
                .orElseThrow(() -> new NotFoundException("Trip event not found with id " + request.getTripEventId()));
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found with id " + request.getEmployeeId()));
        if (!tripEvent.getTrip().getEmployee().getId().equals(employee.getId())) {
            throw new BusinessException("Trip event does not belong to the employee");
        }
        ensureAccess(employee.getId());

        BiometricProvider.VerificationResult result = biometricProvider.verify(employee.getId(), request.getImageBase64());
        BiometricVerification verification = biometricVerificationRepository.findByTripEvent_Id(tripEvent.getId())
                .orElseGet(BiometricVerification::new);
        verification.setTripEvent(tripEvent);
        verification.setEmployee(employee);
        verification.setImageUrl(request.getImageUrl());
        verification.setMatchScore(result.score());
        verification.setVerified(result.verified());
        verification.setProvider(result.providerName());
        verification.setVerifiedAt(LocalDateTime.now());
        verification = biometricVerificationRepository.save(verification);

        tripEvent.setVerificationStatus(result.verified() ? VerificationStatus.VERIFIED : VerificationStatus.FAILED);
        auditLogService.log("BIOMETRIC_VERIFICATION", verification.getId(), "VERIFIED",
                authContextService.currentUserId(), request);
        return biometricVerificationMapper.toResponse(verification);
    }

    @Override
    public BiometricVerificationResponse getById(Long id) {
        BiometricVerification verification = biometricVerificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Biometric verification not found with id " + id));
        ensureAccess(verification.getEmployee().getId());
        return biometricVerificationMapper.toResponse(verification);
    }

    private void ensureAccess(Long employeeId) {
        if (authContextService.currentUserRole() == Role.EMPLOYEE
                && !authContextService.currentUserId().equals(employeeId)) {
            throw new AccessDeniedException("Employees cannot access biometric data of other users");
        }
    }
}
