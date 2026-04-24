package com.company.vacation.dto.trip;

import com.company.vacation.entity.enums.TripEventType;
import com.company.vacation.entity.enums.VerificationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripEventResponse {
    private Long id;
    private Long tripId;
    private TripEventType type;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private LocalDateTime eventTime;
    private VerificationStatus verificationStatus;
    private String comment;
    private LocalDateTime createdAt;
}
