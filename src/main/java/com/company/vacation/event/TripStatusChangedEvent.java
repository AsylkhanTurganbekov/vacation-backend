package com.company.vacation.event;

import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.TripEventType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripStatusChangedEvent {
    private Long tripId;
    private BusinessTripStatus oldStatus;
    private BusinessTripStatus newStatus;
    private Long changedByUserId;
    private LocalDateTime changedAt;
    private TripEventType eventType;
    private LocalDateTime eventTime;
}
