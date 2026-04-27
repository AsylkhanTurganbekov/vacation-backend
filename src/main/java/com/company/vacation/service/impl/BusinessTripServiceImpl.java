package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.trip.CreateTripRequest;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.dto.trip.UpdateTripRequest;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.mapper.BusinessTripMapper;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.BusinessTripService;
import com.company.vacation.specification.BusinessTripSpecification;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessTripServiceImpl implements BusinessTripService {

    private final BusinessTripRepository businessTripRepository;
    private final UserRepository userRepository;
    private final BusinessTripMapper businessTripMapper;
    private final AuditLogService auditLogService;
    private final AuthContextService authContextService;

    @Override
    @Transactional
    public TripResponse createTrip(CreateTripRequest request) {
        validateTripDates(request.getPlannedStartDateTime(), request.getPlannedEndDateTime());
        User employee = findEmployee(request.getEmployeeId());
        BusinessTrip trip = new BusinessTrip();
        trip.setEmployee(employee);
        trip.setPurpose(request.getPurpose());
        trip.setDestinationAddress(request.getDestinationAddress());
        trip.setPlannedStartDateTime(request.getPlannedStartDateTime());
        trip.setPlannedEndDateTime(request.getPlannedEndDateTime());
        trip.setStatus(BusinessTripStatus.DRAFT);
        trip = businessTripRepository.save(trip);
        auditLogService.log("BUSINESS_TRIP", trip.getId(), "CREATED", authContextService.currentUserId(), request);
        return businessTripMapper.toResponse(trip);
    }

    @Override
    public PagedResponse<TripResponse> getTrips(String queryText, BusinessTripStatus status, Long employeeId,
                                                String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        Specification<BusinessTrip> specification =
                BusinessTripSpecification.filter(queryText, status, employeeId, department, dateFrom, dateTo);
        return PagedResponse.from(businessTripRepository.findAll(specification, pageable).map(businessTripMapper::toResponse));
    }

    @Override
    public TripResponse getTrip(Long id) {
        return businessTripMapper.toResponse(findTrip(id));
    }

    @Override
    @Transactional
    public TripResponse updateTrip(Long id, UpdateTripRequest request) {
        validateTripDates(request.getPlannedStartDateTime(), request.getPlannedEndDateTime());
        BusinessTrip trip = findTrip(id);
        if (trip.getStatus() == BusinessTripStatus.COMPLETED || trip.getStatus() == BusinessTripStatus.CANCELLED) {
            throw new BusinessException("Completed or cancelled trip cannot be updated");
        }
        trip.setEmployee(findEmployee(request.getEmployeeId()));
        trip.setPurpose(request.getPurpose());
        trip.setDestinationAddress(request.getDestinationAddress());
        trip.setPlannedStartDateTime(request.getPlannedStartDateTime());
        trip.setPlannedEndDateTime(request.getPlannedEndDateTime());
        auditLogService.log("BUSINESS_TRIP", trip.getId(), "UPDATED", authContextService.currentUserId(), request);
        return businessTripMapper.toResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse approveTrip(Long id) {
        BusinessTrip trip = findTrip(id);
        if (trip.getStatus() != BusinessTripStatus.DRAFT) {
            throw new BusinessException("Only trips in DRAFT status can be approved");
        }
        trip.setStatus(BusinessTripStatus.APPROVED);
        auditLogService.log("BUSINESS_TRIP", trip.getId(), "APPROVED", authContextService.currentUserId(), null);
        return businessTripMapper.toResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse cancelTrip(Long id) {
        BusinessTrip trip = findTrip(id);
        if (trip.getStatus() == BusinessTripStatus.COMPLETED) {
            throw new BusinessException("Completed trip cannot be cancelled");
        }
        if (trip.getStatus() == BusinessTripStatus.CANCELLED) {
            throw new BusinessException("Trip is already cancelled");
        }
        trip.setStatus(BusinessTripStatus.CANCELLED);
        auditLogService.log("BUSINESS_TRIP", trip.getId(), "CANCELLED", authContextService.currentUserId(), null);
        return businessTripMapper.toResponse(trip);
    }

    @Override
    public PagedResponse<TripResponse> getEmployeeTrips(Long employeeId, String queryText, BusinessTripStatus status,
                                                        String department, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        return getTrips(queryText, status, employeeId, department, dateFrom, dateTo, pageable);
    }

    @Override
    public TripResponse getEmployeeTrip(Long employeeId, Long tripId) {
        BusinessTrip trip = businessTripRepository.findByIdAndEmployee_Id(tripId, employeeId)
                .orElseThrow(() -> new NotFoundException("Trip not found with id " + tripId));
        return businessTripMapper.toResponse(trip);
    }

    public BusinessTrip findTrip(Long id) {
        return businessTripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trip not found with id " + id));
    }

    private User findEmployee(Long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id " + employeeId));
    }

    private void validateTripDates(LocalDateTime plannedStart, LocalDateTime plannedEnd) {
        if (plannedEnd.isBefore(plannedStart)) {
            throw new BusinessException("plannedEndDateTime must be after plannedStartDateTime");
        }
    }
}
