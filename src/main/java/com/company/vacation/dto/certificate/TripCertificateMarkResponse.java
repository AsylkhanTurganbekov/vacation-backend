package com.company.vacation.dto.certificate;

import com.company.vacation.entity.enums.TripEventType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripCertificateMarkResponse {
    private TripEventType type;
    private String label;
    private String place;
    private LocalDateTime eventTime;
}
