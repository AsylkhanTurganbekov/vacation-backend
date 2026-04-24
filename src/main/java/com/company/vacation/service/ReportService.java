package com.company.vacation.service;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.report.TripSummaryResponse;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    PagedResponse<TripResponse> getTripsReport(BusinessTripStatus status, Long employeeId,
                                               LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    TripSummaryResponse getSummary();

    PagedResponse<TripResponse> getEmployeeTripsReport(Long employeeId, BusinessTripStatus status,
                                                       LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
}
