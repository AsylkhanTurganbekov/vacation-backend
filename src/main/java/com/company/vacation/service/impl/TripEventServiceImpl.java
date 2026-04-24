package com.company.vacation.service.impl;

import com.company.vacation.dto.trip.TripEventRequest;
import com.company.vacation.dto.trip.TripEventResponse;
import com.company.vacation.entity.BiometricVerification;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.entity.enums.TripEventType;
import com.company.vacation.entity.enums.VerificationStatus;
import com.company.vacation.exception.ApiValidationException;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.mapper.TripEventMapper;
import com.company.vacation.repository.BiometricVerificationRepository;
import com.company.vacation.repository.TripEventRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.BiometricProvider;
import com.company.vacation.service.TripEventService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripEventServiceImpl implements TripEventService {

    private final BusinessTripServiceImpl businessTripService;
    private final TripEventRepository tripEventRepository;
    private final BiometricVerificationRepository biometricVerificationRepository;
    private final BiometricProvider biometricProvider;
    private final TripEventMapper tripEventMapper;
    private final AuthContextService authContextService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TripEventResponse createEvent(Long tripId, TripEventType type, TripEventRequest request) {
        validateImagePayload(request);
        BusinessTrip trip = businessTripService.findTrip(tripId);
        ensureTripAccess(trip);
        validateBusinessFlow(trip, type);

        TripEvent event = new TripEvent();
        event.setTrip(trip);
        event.setType(type);
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setAddress(request.getAddress());
        event.setEventTime(request.getEventTime());
        event.setComment(request.getComment());
        event.setVerificationStatus(VerificationStatus.PENDING);
        event = tripEventRepository.save(event);

        persistBiometricVerification(trip, event, request);
        updateTripStatus(trip, type, request.getEventTime());
        auditLogService.log("TRIP_EVENT", event.getId(), "CREATED", authContextService.currentUserId(), request);
        return tripEventMapper.toResponse(event);
    }

    @Override
    public List<TripEventResponse> getTripEvents(Long tripId) {
        BusinessTrip trip = businessTripService.findTrip(tripId);
        ensureTripAccess(trip);
        return tripEventRepository.findByTrip_IdOrderByEventTimeAsc(tripId).stream()
                .map(tripEventMapper::toResponse)
                .toList();
    }

    private void ensureTripAccess(BusinessTrip trip) {
        if (authContextService.currentUserRole() == Role.EMPLOYEE
                && !trip.getEmployee().getId().equals(authContextService.currentUserId())) {
            throw new BusinessException("Employees can only operate on their own trips");
        }
    }

    private void validateBusinessFlow(BusinessTrip trip, TripEventType type) {
        if (trip.getStatus() == BusinessTripStatus.CANCELLED || trip.getStatus() == BusinessTripStatus.COMPLETED) {
            throw new BusinessException("Cannot create event for completed or cancelled trip");
        }

        switch (type) {
            case DEPARTURE -> {
                if (trip.getStatus() != BusinessTripStatus.APPROVED) {
                    throw new BusinessException("DEPARTURE is allowed only for APPROVED trips");
                }
            }
            case ARRIVAL -> {
                if (trip.getStatus() != BusinessTripStatus.IN_PROGRESS
                        || !tripEventRepository.existsByTrip_IdAndType(trip.getId(), TripEventType.DEPARTURE)) {
                    throw new BusinessException("ARRIVAL requires existing DEPARTURE");
                }
            }
            case RETURN -> {
                if (trip.getStatus() != BusinessTripStatus.ARRIVED
                        || !tripEventRepository.existsByTrip_IdAndType(trip.getId(), TripEventType.ARRIVAL)) {
                    throw new BusinessException("RETURN requires existing ARRIVAL");
                }
            }
        }
    }

    private void validateImagePayload(TripEventRequest request) {
        boolean hasBase64 = request.getImageBase64() != null && !request.getImageBase64().isBlank();
        boolean hasUrl = request.getImageUrl() != null && !request.getImageUrl().isBlank();
        if (!hasBase64 && !hasUrl) {
            throw new ApiValidationException("Either imageBase64 or imageUrl must be provided");
        }
    }

    private void persistBiometricVerification(BusinessTrip trip, TripEvent event, TripEventRequest request) {
        String payload = request.getImageBase64() != null && !request.getImageBase64().isBlank()
                ? request.getImageBase64()
                : request.getImageUrl();
        BiometricProvider.VerificationResult result = biometricProvider.verify(trip.getEmployee().getId(), payload);

        event.setVerificationStatus(result.verified() ? VerificationStatus.VERIFIED : VerificationStatus.FAILED);
        BiometricVerification verification = new BiometricVerification();
        verification.setTripEvent(event);
        verification.setEmployee(trip.getEmployee());
        verification.setImageUrl(request.getImageUrl());
        verification.setMatchScore(result.score());
        verification.setVerified(result.verified());
        verification.setProvider(result.providerName());
        verification.setVerifiedAt(LocalDateTime.now());
        biometricVerificationRepository.save(verification);
    }

    private void updateTripStatus(BusinessTrip trip, TripEventType type, LocalDateTime eventTime) {
        switch (type) {
            case DEPARTURE -> {
                trip.setStatus(BusinessTripStatus.IN_PROGRESS);
                trip.setActualStartDateTime(eventTime);
            }
            case ARRIVAL -> {
                trip.setStatus(BusinessTripStatus.ARRIVED);
                trip.setActualArrivalDateTime(eventTime);
            }
            case RETURN -> {
                trip.setStatus(BusinessTripStatus.COMPLETED);
                trip.setActualReturnDateTime(eventTime);
            }
        }
    }
}
