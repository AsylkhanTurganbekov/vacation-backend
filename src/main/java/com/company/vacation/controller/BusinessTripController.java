package com.company.vacation.controller;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.trip.CreateTripRequest;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.dto.trip.UpdateTripRequest;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.service.BusinessTripService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class BusinessTripController {

    private final BusinessTripService businessTripService;

    @PostMapping
    public TripResponse createTrip(@Valid @RequestBody CreateTripRequest request) {
        return businessTripService.createTrip(request);
    }

    @GetMapping
    public PagedResponse<TripResponse> getTrips(@RequestParam(required = false) BusinessTripStatus status,
                                                @RequestParam(required = false) Long employeeId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                                                @PageableDefault(size = 20, sort = "plannedStartDateTime") Pageable pageable) {
        return businessTripService.getTrips(status, employeeId, dateFrom, dateTo, pageable);
    }

    @GetMapping("/{id}")
    public TripResponse getTrip(@PathVariable Long id) {
        return businessTripService.getTrip(id);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(@PathVariable Long id, @Valid @RequestBody UpdateTripRequest request) {
        return businessTripService.updateTrip(id, request);
    }

    @PatchMapping("/{id}/approve")
    public TripResponse approveTrip(@PathVariable Long id) {
        return businessTripService.approveTrip(id);
    }

    @PatchMapping("/{id}/cancel")
    public TripResponse cancelTrip(@PathVariable Long id) {
        return businessTripService.cancelTrip(id);
    }
}
