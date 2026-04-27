package com.company.vacation.dto.monitoring;

import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.TripEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitoringMapPointResponse {
    private Long tripId;
    private Long employeeId;
    private String employeeName;
    private String employeeAvatarUrl;
    private String department;
    private String purpose;
    private String destinationAddress;
    private String currentAddress;
    private BusinessTripStatus status;
    private TripEventType lastEventType;
    private LocalDateTime lastEventTime;
    private List<BigDecimal> coordinates;
}
