package com.company.vacation.service;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.trip.CreateTripRequest;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.dto.trip.UpdateTripRequest;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public interface BusinessTripService {

    TripResponse createTrip(CreateTripRequest request);

    PagedResponse<TripResponse> getTrips(String queryText, BusinessTripStatus status, Long employeeId,
                                         String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    TripResponse getTrip(Long id);

    TripResponse updateTrip(Long id, UpdateTripRequest request);

    TripResponse approveTrip(Long id);

    TripResponse cancelTrip(Long id);

    PagedResponse<TripResponse> getEmployeeTrips(Long employeeId, String queryText, BusinessTripStatus status,
                                                 String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    TripResponse getEmployeeTrip(Long employeeId, Long tripId);
}
