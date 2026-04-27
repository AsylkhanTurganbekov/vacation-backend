package com.company.vacation.service.impl;

import com.company.vacation.dto.monitoring.MonitoringMapPointResponse;
import com.company.vacation.dto.monitoring.MonitoringMapResponse;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.mapper.UserMapper;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.TripEventRepository;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.MonitoringService;
import com.company.vacation.specification.BusinessTripSpecification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final BusinessTripRepository businessTripRepository;
    private final TripEventRepository tripEventRepository;
    private final AuthContextService authContextService;
    private final UserMapper userMapper;

    @Override
    public MonitoringMapResponse getMapData(String queryText, Long employeeId, List<Long> employeeIds, String department,
                                            BusinessTripStatus status, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<Long> effectiveEmployeeIds = authContextService.currentUserRole() == Role.EMPLOYEE
                ? List.of(authContextService.currentUserId())
                : mergeEmployeeFilters(employeeId, employeeIds);

        List<BusinessTrip> trips = businessTripRepository.findAll(
                BusinessTripSpecification.filter(queryText, status, effectiveEmployeeIds, department, dateFrom, dateTo),
                Sort.by(Sort.Direction.DESC, "plannedStartDateTime"));

        List<MonitoringMapPointResponse> withCoordinates = new ArrayList<>();
        List<MonitoringMapPointResponse> withoutCoordinates = new ArrayList<>();

        for (BusinessTrip trip : trips) {
            MonitoringMapPointResponse point = toMapPoint(
                    trip,
                    tripEventRepository.findTopByTrip_IdOrderByEventTimeDescCreatedAtDesc(trip.getId()).orElse(null)
            );
            if (point.getCoordinates() == null) {
                withoutCoordinates.add(point);
            } else {
                withCoordinates.add(point);
            }
        }

        return MonitoringMapResponse.builder()
                .withCoordinates(withCoordinates)
                .withoutCoordinates(withoutCoordinates)
                .build();
    }

    private List<Long> mergeEmployeeFilters(Long employeeId, List<Long> employeeIds) {
        if (employeeId != null) {
            return List.of(employeeId);
        }
        if (employeeIds == null || employeeIds.isEmpty()) {
            return null;
        }
        return employeeIds.stream().distinct().toList();
    }

    private MonitoringMapPointResponse toMapPoint(BusinessTrip trip, TripEvent latestEvent) {
        MonitoringMapPointResponse response = new MonitoringMapPointResponse();
        response.setTripId(trip.getId());
        response.setEmployeeId(trip.getEmployee().getId());
        response.setEmployeeName(trip.getEmployee().getFullName());
        response.setEmployeeAvatarUrl(userMapper.toResponse(trip.getEmployee()).getAvatarUrl());
        response.setDepartment(trip.getEmployee().getDepartment());
        response.setPurpose(trip.getPurpose());
        response.setDestinationAddress(trip.getDestinationAddress());
        response.setStatus(trip.getStatus());

        if (latestEvent != null) {
            response.setCurrentAddress(latestEvent.getAddress());
            response.setLastEventType(latestEvent.getType());
            response.setLastEventTime(latestEvent.getEventTime());
            response.setCoordinates(List.of(latestEvent.getLongitude(), latestEvent.getLatitude()));
        }

        return response;
    }
}
