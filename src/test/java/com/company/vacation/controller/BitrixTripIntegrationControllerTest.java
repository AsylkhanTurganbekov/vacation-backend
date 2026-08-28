package com.company.vacation.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.vacation.service.BitrixTripIntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class BitrixTripIntegrationControllerTest {

    @Test
    void rejectsRequestsWhenIntegrationSecretIsNotConfigured() {
        BitrixTripIntegrationController controller = new BitrixTripIntegrationController(org.mockito.Mockito.mock(BitrixTripIntegrationService.class));
        ReflectionTestUtils.setField(controller, "apiKey", "");

        assertThatThrownBy(() -> controller.getTrip("provided", "external-1"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void rejectsIncorrectIntegrationSecret() {
        BitrixTripIntegrationController controller = new BitrixTripIntegrationController(org.mockito.Mockito.mock(BitrixTripIntegrationService.class));
        ReflectionTestUtils.setField(controller, "apiKey", "expected-secret");

        assertThatThrownBy(() -> controller.getTrip("wrong-secret", "external-1"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
