package com.company.vacation.service;

import com.company.vacation.dto.monitoring.MonitoringMapResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;

public interface MonitoringService {

    MonitoringMapResponse getMapData(String queryText, Long employeeId, String department,
                                     BusinessTripStatus status, LocalDateTime dateFrom, LocalDateTime dateTo);
}
