package com.company.vacation.controller;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.BusinessTripService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee/trips")
@RequiredArgsConstructor
public class EmployeeTripController {

    private final BusinessTripService businessTripService;
    private final AuthContextService authContextService;

    @GetMapping
    public PagedResponse<TripResponse> getEmployeeTrips(@RequestParam(required = false) BusinessTripStatus status,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                                                        @PageableDefault(size = 20, sort = "plannedStartDateTime") Pageable pageable) {
        return businessTripService.getEmployeeTrips(authContextService.currentUserId(), status, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{id}")
    public TripResponse getEmployeeTrip(@PathVariable Long id) {
        return businessTripService.getEmployeeTrip(authContextService.currentUserId(), id);
    }
}
