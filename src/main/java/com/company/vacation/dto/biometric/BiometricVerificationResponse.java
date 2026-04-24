package com.company.vacation.dto.biometric;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BiometricVerificationResponse {
    private Long id;
    private Long tripEventId;
    private Long employeeId;
    private String imageUrl;
    private BigDecimal matchScore;
    private boolean verified;
    private String provider;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
