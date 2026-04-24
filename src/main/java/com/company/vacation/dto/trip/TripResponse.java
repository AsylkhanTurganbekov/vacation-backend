package com.company.vacation.dto.trip;

import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String purpose;
    private String destinationAddress;
    private LocalDateTime plannedStartDateTime;
    private LocalDateTime plannedEndDateTime;
    private LocalDateTime actualStartDateTime;
    private LocalDateTime actualArrivalDateTime;
    private LocalDateTime actualReturnDateTime;
    private BusinessTripStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
