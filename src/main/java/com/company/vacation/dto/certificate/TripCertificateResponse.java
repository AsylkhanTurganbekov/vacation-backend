package com.company.vacation.dto.certificate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripCertificateResponse {
    private Long tripId;
    private String organizationName;
    private String documentNumber;
    private LocalDate documentDate;
    private String employeeFullName;
    private String department;
    private String position;
    private String personnelNumber;
    private String destinationAddress;
    private String purpose;
    private LocalDateTime plannedStartDateTime;
    private LocalDateTime plannedEndDateTime;
    private long calendarDays;
    private List<TripCertificateMarkResponse> marks;
}
