package com.company.vacation.controller;

import com.company.vacation.dto.trip.TripEventRequest;
import com.company.vacation.dto.trip.TripEventResponse;
import com.company.vacation.entity.enums.TripEventType;
import com.company.vacation.service.TripEventService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/events")
@RequiredArgsConstructor
public class TripEventController {

    private final TripEventService tripEventService;

    @PostMapping("/departure")
    public TripEventResponse departure(@PathVariable Long tripId, @Valid @RequestBody TripEventRequest request) {
        return tripEventService.createEvent(tripId, TripEventType.DEPARTURE, request);
    }

    @PostMapping("/arrival")
    public TripEventResponse arrival(@PathVariable Long tripId, @Valid @RequestBody TripEventRequest request) {
        return tripEventService.createEvent(tripId, TripEventType.ARRIVAL, request);
    }

    @PostMapping("/return")
    public TripEventResponse returnTrip(@PathVariable Long tripId, @Valid @RequestBody TripEventRequest request) {
        return tripEventService.createEvent(tripId, TripEventType.RETURN, request);
    }

    @GetMapping
    public List<TripEventResponse> getEvents(@PathVariable Long tripId) {
        return tripEventService.getTripEvents(tripId);
    }
}
