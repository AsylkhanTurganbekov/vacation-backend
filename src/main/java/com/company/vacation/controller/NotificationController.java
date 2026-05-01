package com.company.vacation.controller;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.notification.NotificationResponse;
import com.company.vacation.service.NotificationCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationCenterService notificationCenterService;

    @GetMapping
    public PagedResponse<NotificationResponse> getNotifications(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return notificationCenterService.getCurrentUserNotifications(pageable);
    }

    @PatchMapping("/{id}/read")
    public SuccessResponse markRead(@PathVariable Long id) {
        return notificationCenterService.markAsRead(id);
    }

    @PatchMapping("/read-all")
    public SuccessResponse markAllRead() {
        return notificationCenterService.markAllAsRead();
    }
}
