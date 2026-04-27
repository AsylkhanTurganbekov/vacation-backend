package com.company.vacation.service.impl;

import com.company.vacation.dto.certificate.TripCertificateMarkResponse;
import com.company.vacation.dto.certificate.TripCertificateResponse;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.entity.enums.TripEventType;
import com.company.vacation.exception.BusinessException;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.TripCertificateService;
import com.company.vacation.service.TripEventService;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class TripCertificateServiceImpl implements TripCertificateService {

    private final BusinessTripServiceImpl businessTripService;
    private final TripEventServiceImpl tripEventService;
    private final AuthContextService authContextService;
    private final TemplateEngine templateEngine;

    @Value("${app.documents.organization-name:${APP_DOCUMENTS_ORGANIZATION_NAME:АО \"Национальные информационные технологии\"}}")
    private String organizationName;

    @Override
    public TripCertificateResponse getCertificate(Long tripId) {
        BusinessTrip trip = businessTripService.findTrip(tripId);
        ensureTripAccess(trip);

        List<TripCertificateMarkResponse> marks = tripEventService.findTripEventsForTrip(tripId).stream()
                .map(this::toMark)
                .toList();

        long calendarDays = ChronoUnit.DAYS.between(
                trip.getPlannedStartDateTime().toLocalDate(),
                trip.getPlannedEndDateTime().toLocalDate()
        ) + 1;

        return TripCertificateResponse.builder()
                .tripId(trip.getId())
                .organizationName(organizationName)
                .documentNumber(buildDocumentNumber(trip))
                .documentDate(trip.getCreatedAt().toLocalDate())
                .employeeFullName(trip.getEmployee().getFullName())
                .department(trip.getEmployee().getDepartment())
                .position(trip.getEmployee().getPosition())
                .personnelNumber(trip.getEmployee().getId().toString())
                .destinationAddress(trip.getDestinationAddress())
                .purpose(trip.getPurpose())
                .plannedStartDateTime(trip.getPlannedStartDateTime())
                .plannedEndDateTime(trip.getPlannedEndDateTime())
                .calendarDays(calendarDays)
                .marks(marks)
                .build();
    }

    @Override
    public String renderCertificateHtml(Long tripId) {
        TripCertificateResponse certificate = getCertificate(tripId);
        Context context = new Context();
        context.setVariable("certificate", certificate);
        return templateEngine.process("trip-certificate", context);
    }

    private void ensureTripAccess(BusinessTrip trip) {
        if (authContextService.currentUserRole() == Role.EMPLOYEE
                && !trip.getEmployee().getId().equals(authContextService.currentUserId())) {
            throw new BusinessException("Employees can only access their own trip certificates");
        }
    }

    private TripCertificateMarkResponse toMark(TripEvent event) {
        return TripCertificateMarkResponse.builder()
                .type(event.getType())
                .label(labelFor(event.getType()))
                .place(event.getAddress())
                .eventTime(event.getEventTime())
                .build();
    }

    private String labelFor(TripEventType type) {
        return switch (type) {
            case DEPARTURE -> "Выбыл из места постоянной работы";
            case ARRIVAL -> "Прибыл в пункт назначения";
            case RETURN -> "Прибыл в место постоянной работы";
        };
    }

    private String buildDocumentNumber(BusinessTrip trip) {
        return "KU-%d-%d".formatted(trip.getCreatedAt().getYear(), trip.getId());
    }
}
