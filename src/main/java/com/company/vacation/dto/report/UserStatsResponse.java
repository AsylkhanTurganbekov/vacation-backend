package com.company.vacation.dto.report;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserStatsResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String position;
    private String avatarUrl;
    private boolean active;
    private long totalTrips;
    private Map<String, Long> tripsByStatus;
    private long totalEvents;
    private long verifiedEvents;
    private long failedEvents;
    private long pendingEvents;
    private LocalDateTime lastTripAt;
    private LocalDateTime lastEventAt;
}
