package com.company.vacation.service;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.notification.NotificationResponse;
import com.company.vacation.event.TripStatusChangedEvent;
import org.springframework.data.domain.Pageable;

public interface NotificationCenterService {

    PagedResponse<NotificationResponse> getCurrentUserNotifications(Pageable pageable);

    SuccessResponse markAsRead(Long notificationId);

    SuccessResponse markAllAsRead();

    void processTripStatusChangedEvent(TripStatusChangedEvent event);
}
