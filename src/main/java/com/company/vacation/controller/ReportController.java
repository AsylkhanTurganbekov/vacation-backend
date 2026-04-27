package com.company.vacation.controller;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.report.TripSummaryResponse;
import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.service.ReportService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/trips")
    public PagedResponse<TripResponse> getTripsReport(@RequestParam(required = false, name = "q") String queryText,
                                                      @RequestParam(required = false) BusinessTripStatus status,
                                                      @RequestParam(required = false) Long employeeId,
                                                      @RequestParam(required = false) String department,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                                                      @PageableDefault(size = 20, sort = "plannedStartDateTime") Pageable pageable) {
        return reportService.getTripsReport(queryText, status, employeeId, department, dateFrom, dateTo, pageable);
    }

    @GetMapping("/trips/summary")
    public TripSummaryResponse getSummary() {
        return reportService.getSummary();
    }

    @GetMapping("/employees/{employeeId}/trips")
    public PagedResponse<TripResponse> getEmployeeTripsReport(@PathVariable Long employeeId,
                                                              @RequestParam(required = false, name = "q") String queryText,
                                                              @RequestParam(required = false) BusinessTripStatus status,
                                                              @RequestParam(required = false) String department,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                                                              @PageableDefault(size = 20, sort = "plannedStartDateTime") Pageable pageable) {
        return reportService.getEmployeeTripsReport(employeeId, queryText, status, department, dateFrom, dateTo, pageable);
    }
}
