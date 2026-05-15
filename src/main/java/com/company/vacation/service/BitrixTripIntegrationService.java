package com.company.vacation.service;

import com.company.vacation.dto.integration.bitrix.BitrixTripUpsertRequest;
import com.company.vacation.dto.trip.TripResponse;

public interface BitrixTripIntegrationService {

    TripResponse upsertTrip(BitrixTripUpsertRequest request);

    TripResponse getTripByExternalTripId(String externalTripId);
}
