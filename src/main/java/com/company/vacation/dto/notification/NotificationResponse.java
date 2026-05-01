package com.company.vacation.dto.notification;

import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String body;
    private Long tripId;
    private String clickAction;
    private BusinessTripStatus oldStatus;
    private BusinessTripStatus newStatus;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
