package com.company.vacation.service;

import com.company.vacation.dto.trip.TripEventRequest;
import com.company.vacation.dto.trip.TripEventResponse;
import com.company.vacation.entity.enums.TripEventType;
import java.util.List;

public interface TripEventService {

    TripEventResponse createEvent(Long tripId, TripEventType type, TripEventRequest request);

    List<TripEventResponse> getTripEvents(Long tripId);
}
