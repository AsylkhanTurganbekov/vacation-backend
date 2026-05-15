package com.company.vacation.service.impl;

import com.company.vacation.dto.integration.bitrix.BitrixTripUpsertRequest;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.mapper.BusinessTripMapper;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.BitrixTripIntegrationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BitrixTripIntegrationServiceImpl implements BitrixTripIntegrationService {

    private final BusinessTripRepository businessTripRepository;
    private final UserRepository userRepository;
    private final BusinessTripMapper businessTripMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public TripResponse upsertTrip(BitrixTripUpsertRequest request) {
        validateTripDates(request.getPlannedStartDateTime(), request.getPlannedEndDateTime());
        BusinessTrip trip = businessTripRepository.findByExternalTripId(request.getExternalTripId())
                .orElseGet(BusinessTrip::new);

        boolean created = trip.getId() == null;
        if (!created && (trip.getStatus() == BusinessTripStatus.COMPLETED || trip.getStatus() == BusinessTripStatus.CANCELLED)) {
            throw new BusinessException("Completed or cancelled trip cannot be updated via Bitrix integration");
        }

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found with id " + request.getEmployeeId()));

        trip.setExternalTripId(request.getExternalTripId());
        trip.setEmployee(employee);
        trip.setPurpose(request.getPurpose());
        trip.setDestinationAddress(request.getDestinationAddress());
        trip.setPlannedStartDateTime(request.getPlannedStartDateTime());
        trip.setPlannedEndDateTime(request.getPlannedEndDateTime());
        if (created) {
            trip.setStatus(BusinessTripStatus.DRAFT);
        }

        trip = businessTripRepository.save(trip);
        auditLogService.log("BUSINESS_TRIP", trip.getId(), created ? "BITRIX_CREATED" : "BITRIX_UPDATED", null, request);
        return businessTripMapper.toResponse(trip);
    }

    @Override
    public TripResponse getTripByExternalTripId(String externalTripId) {
        BusinessTrip trip = businessTripRepository.findByExternalTripId(externalTripId)
                .orElseThrow(() -> new NotFoundException("Trip not found with externalTripId " + externalTripId));
        return businessTripMapper.toResponse(trip);
    }

    private void validateTripDates(LocalDateTime plannedStart, LocalDateTime plannedEnd) {
        if (plannedEnd.isBefore(plannedStart)) {
            throw new BusinessException("plannedEndDateTime must be after plannedStartDateTime");
        }
    }
}
