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

    PagedResponse<TripResponse> getTrips(BusinessTripStatus status, Long employeeId,
                                         LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    TripResponse getTrip(Long id);

    TripResponse updateTrip(Long id, UpdateTripRequest request);

    TripResponse approveTrip(Long id);

    TripResponse cancelTrip(Long id);

    PagedResponse<TripResponse> getEmployeeTrips(Long employeeId, BusinessTripStatus status,
                                                 LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    TripResponse getEmployeeTrip(Long employeeId, Long tripId);
}
