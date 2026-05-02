package com.company.vacation.service;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.notification.NotificationResponse;
import com.company.vacation.dto.notification.TestPushRequest;
import com.company.vacation.dto.notification.TestPushResponse;
import com.company.vacation.event.TripStatusChangedEvent;
import org.springframework.data.domain.Pageable;

public interface NotificationCenterService {

    PagedResponse<NotificationResponse> getCurrentUserNotifications(Pageable pageable);

    SuccessResponse markAsRead(Long notificationId);

    SuccessResponse markAllAsRead();

    TestPushResponse sendTestPush(TestPushRequest request);

    void processTripStatusChangedEvent(TripStatusChangedEvent event);
}
