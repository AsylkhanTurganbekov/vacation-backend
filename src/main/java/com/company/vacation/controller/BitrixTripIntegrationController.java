package com.company.vacation.controller;

import com.company.vacation.dto.integration.bitrix.BitrixTripUpsertRequest;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.service.BitrixTripIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/integrations/bitrix/trips")
@RequiredArgsConstructor
public class BitrixTripIntegrationController {

    private final BitrixTripIntegrationService bitrixTripIntegrationService;

    @Value("${app.integrations.bitrix.api-key:}")
    private String apiKey;

    @PostMapping
    public TripResponse upsertTrip(@RequestHeader(name = "X-API-Key", required = false) String requestApiKey,
                                   @Valid @RequestBody BitrixTripUpsertRequest request) {
        validateApiKey(requestApiKey);
        return bitrixTripIntegrationService.upsertTrip(request);
    }

    @GetMapping("/{externalTripId}")
    public TripResponse getTrip(@RequestHeader(name = "X-API-Key", required = false) String requestApiKey,
                                @PathVariable String externalTripId) {
        validateApiKey(requestApiKey);
        return bitrixTripIntegrationService.getTripByExternalTripId(externalTripId);
    }

    private void validateApiKey(String requestApiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Bitrix integration API key is not configured");
        }
        if (requestApiKey == null || requestApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-API-Key header");
        }
        if (!apiKey.equals(requestApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid X-API-Key");
        }
    }
}
