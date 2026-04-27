package com.company.vacation.controller;

import com.company.vacation.dto.monitoring.MonitoringMapResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.service.MonitoringService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/map")
    public MonitoringMapResponse getMapData(@RequestParam(required = false, name = "q") String queryText,
                                            @RequestParam(required = false) Long employeeId,
                                            @RequestParam(required = false) String department,
                                            @RequestParam(required = false) BusinessTripStatus status,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return monitoringService.getMapData(queryText, employeeId, department, status, dateFrom, dateTo);
    }
}
