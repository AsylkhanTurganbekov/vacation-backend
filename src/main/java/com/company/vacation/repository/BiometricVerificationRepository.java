package com.company.vacation.repository;

import com.company.vacation.entity.BiometricVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricVerificationRepository extends JpaRepository<BiometricVerification, Long> {

    java.util.Optional<BiometricVerification> findByTripEvent_Id(Long tripEventId);
}
