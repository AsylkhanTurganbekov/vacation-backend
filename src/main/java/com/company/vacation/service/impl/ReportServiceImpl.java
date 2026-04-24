package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.report.TripSummaryResponse;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.VerificationStatus;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.TripEventRepository;
import com.company.vacation.service.ReportService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BusinessTripServiceImpl businessTripService;
    private final BusinessTripRepository businessTripRepository;
    private final TripEventRepository tripEventRepository;

    @Override
    public PagedResponse<TripResponse> getTripsReport(BusinessTripStatus status, Long employeeId,
                                                      LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return businessTripService.getTrips(status, employeeId, dateFrom, dateTo, pageable);
    }

    @Override
    public TripSummaryResponse getSummary() {
        Map<String, Long> tripsByStatus = Arrays.stream(BusinessTripStatus.values())
                .collect(Collectors.toMap(Enum::name,
                        status -> businessTripRepository.count((root, query, cb) -> cb.equal(root.get("status"), status))));

        return TripSummaryResponse.builder()
                .totalTrips(businessTripRepository.count())
                .tripsByStatus(tripsByStatus)
                .verifiedEvents(tripEventRepository.countByVerificationStatus(VerificationStatus.VERIFIED))
                .failedEvents(tripEventRepository.countByVerificationStatus(VerificationStatus.FAILED))
                .pendingEvents(tripEventRepository.countByVerificationStatus(VerificationStatus.PENDING))
                .build();
    }

    @Override
    public PagedResponse<TripResponse> getEmployeeTripsReport(Long employeeId, BusinessTripStatus status,
                                                              LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return businessTripService.getEmployeeTrips(employeeId, status, dateFrom, dateTo, pageable);
    }
}
