package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.report.TripSummaryResponse;
import com.company.vacation.dto.report.UserStatsResponse;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.VerificationStatus;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.TripEventRepository;
import com.company.vacation.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public PagedResponse<TripResponse> getTripsReport(String queryText, BusinessTripStatus status, Long employeeId,
                                                      String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return businessTripService.getTrips(queryText, status, employeeId, department, dateFrom, dateTo, pageable);
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
    public PagedResponse<TripResponse> getEmployeeTripsReport(Long employeeId, String queryText, BusinessTripStatus status,
                                                              String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return businessTripService.getEmployeeTrips(employeeId, queryText, status, department, dateFrom, dateTo, pageable);
    }

    @Override
    public UserStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.company.vacation.exception.NotFoundException("User not found with id " + userId));

        Map<String, Long> tripsByStatus = Arrays.stream(BusinessTripStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> businessTripRepository.countByEmployee_IdAndStatus(userId, status)
                ));

        BusinessTrip lastTrip = businessTripRepository.findTopByEmployee_IdOrderByPlannedStartDateTimeDesc(userId)
                .orElse(null);
        TripEvent lastEvent = tripEventRepository.findTopByTrip_Employee_IdOrderByEventTimeDescCreatedAtDesc(userId)
                .orElse(null);

        return UserStatsResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .position(user.getPosition())
                .avatarUrl(user.getAvatarFileName() != null ? "/api/v1/users/" + user.getId() + "/avatar" : null)
                .active(user.isActive())
                .totalTrips(businessTripRepository.countByEmployee_Id(userId))
                .tripsByStatus(tripsByStatus)
                .totalEvents(tripEventRepository.countByTrip_Employee_Id(userId))
                .verifiedEvents(tripEventRepository.countByTrip_Employee_IdAndVerificationStatus(userId, VerificationStatus.VERIFIED))
                .failedEvents(tripEventRepository.countByTrip_Employee_IdAndVerificationStatus(userId, VerificationStatus.FAILED))
                .pendingEvents(tripEventRepository.countByTrip_Employee_IdAndVerificationStatus(userId, VerificationStatus.PENDING))
                .lastTripAt(lastTrip != null ? lastTrip.getPlannedStartDateTime() : null)
                .lastEventAt(lastEvent != null ? lastEvent.getEventTime() : null)
                .build();
    }
}
