package com.company.vacation.service;

import com.company.vacation.dto.monitoring.MonitoringMapResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface MonitoringService {

    MonitoringMapResponse getMapData(String queryText, Long employeeId, List<Long> employeeIds, String department,
                                     BusinessTripStatus status, LocalDateTime dateFrom, LocalDateTime dateTo);
}
