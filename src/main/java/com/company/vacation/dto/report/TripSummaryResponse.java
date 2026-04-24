package com.company.vacation.dto.report;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripSummaryResponse {
    private long totalTrips;
    private Map<String, Long> tripsByStatus;
    private long verifiedEvents;
    private long failedEvents;
    private long pendingEvents;
}
