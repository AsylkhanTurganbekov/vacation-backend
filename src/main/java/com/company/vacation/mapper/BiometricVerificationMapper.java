package com.company.vacation.mapper;

import com.company.vacation.dto.biometric.BiometricVerificationResponse;
import com.company.vacation.entity.BiometricVerification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BiometricVerificationMapper {

    @Mapping(target = "tripEventId", source = "tripEvent.id")
    @Mapping(target = "employeeId", source = "employee.id")
    BiometricVerificationResponse toResponse(BiometricVerification biometricVerification);
}
