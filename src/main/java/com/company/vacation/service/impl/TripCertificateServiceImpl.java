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

@Service
@RequiredArgsConstructor
public class TripCertificateServiceImpl implements TripCertificateService {

    private final BusinessTripServiceImpl businessTripService;
    private final TripEventServiceImpl tripEventService;
    private final AuthContextService authContextService;

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
        String marksHtml = certificate.getMarks().stream()
                .map(mark -> """
                        <tr>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                        </tr>
                        """.formatted(
                        escapeHtml(mark.getLabel()),
                        escapeHtml(mark.getPlace()),
                        mark.getEventTime() != null ? mark.getEventTime().toString() : ""
                ))
                .reduce("", String::concat);

        return """
                <!doctype html>
                <html lang="ru">
                <head>
                  <meta charset="UTF-8">
                  <title>Командировочное удостоверение</title>
                  <style>
                    body { font-family: Arial, sans-serif; margin: 32px; color: #111; }
                    h1, h2 { margin: 0 0 12px; }
                    .header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:24px; }
                    .meta { border:1px solid #222; padding:12px; min-width:260px; }
                    .field { margin: 8px 0; }
                    .label { font-weight:700; }
                    table { width:100%%; border-collapse:collapse; margin-top:16px; }
                    th, td { border:1px solid #444; padding:8px; text-align:left; vertical-align:top; }
                    .muted { color:#555; font-size:12px; }
                  </style>
                </head>
                <body>
                  <div class="header">
                    <div>
                      <div class="muted">%s</div>
                      <h1>Командировочное удостоверение</h1>
                    </div>
                    <div class="meta">
                      <div><span class="label">Номер документа:</span> %s</div>
                      <div><span class="label">Дата составления:</span> %s</div>
                    </div>
                  </div>
                  <div class="field"><span class="label">Работник:</span> %s</div>
                  <div class="field"><span class="label">Подразделение:</span> %s</div>
                  <div class="field"><span class="label">Должность:</span> %s</div>
                  <div class="field"><span class="label">Табельный номер:</span> %s</div>
                  <div class="field"><span class="label">Командируется в:</span> %s</div>
                  <div class="field"><span class="label">Цель:</span> %s</div>
                  <div class="field"><span class="label">Период:</span> с %s по %s</div>
                  <div class="field"><span class="label">Календарных дней:</span> %d</div>

                  <h2>Отметки по поездке</h2>
                  <table>
                    <thead>
                      <tr>
                        <th>Тип</th>
                        <th>Место</th>
                        <th>Дата и время</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(certificate.getOrganizationName()),
                escapeHtml(certificate.getDocumentNumber()),
                certificate.getDocumentDate(),
                escapeHtml(certificate.getEmployeeFullName()),
                escapeHtml(certificate.getDepartment()),
                escapeHtml(certificate.getPosition()),
                escapeHtml(certificate.getPersonnelNumber()),
                escapeHtml(certificate.getDestinationAddress()),
                escapeHtml(certificate.getPurpose()),
                certificate.getPlannedStartDateTime(),
                certificate.getPlannedEndDateTime(),
                certificate.getCalendarDays(),
                marksHtml
        );
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

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
