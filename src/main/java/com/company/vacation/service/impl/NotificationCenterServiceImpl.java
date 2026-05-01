package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.notification.NotificationResponse;
import com.company.vacation.entity.AuditLog;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.User;
import com.company.vacation.entity.UserDevice;
import com.company.vacation.entity.UserNotification;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.NotificationType;
import com.company.vacation.event.TripStatusChangedEvent;
import com.company.vacation.exception.NotFoundException;
import com.company.vacation.repository.AuditLogRepository;
import com.company.vacation.repository.UserNotificationRepository;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.DevicePushTokenService;
import com.company.vacation.service.NotificationCenterService;
import com.company.vacation.service.PushDeliveryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCenterServiceImpl implements NotificationCenterService {

    private final UserNotificationRepository userNotificationRepository;
    private final DevicePushTokenService devicePushTokenService;
    private final PushDeliveryService pushDeliveryService;
    private final AuthContextService authContextService;
    private final AuditLogRepository auditLogRepository;
    private final BusinessTripServiceImpl businessTripService;
    private final ObjectMapper objectMapper;

    @Override
    public PagedResponse<NotificationResponse> getCurrentUserNotifications(Pageable pageable) {
        return PagedResponse.from(userNotificationRepository.findByUser_IdOrderByCreatedAtDesc(
                authContextService.currentUserId(), pageable).map(this::toResponse));
    }

    @Override
    @Transactional
    public SuccessResponse markAsRead(Long notificationId) {
        UserNotification notification = userNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id " + notificationId));
        if (!notification.getUser().getId().equals(authContextService.currentUserId())) {
            throw new NotFoundException("Notification not found with id " + notificationId);
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return SuccessResponse.builder().success(true).build();
    }

    @Override
    @Transactional
    public SuccessResponse markAllAsRead() {
        userNotificationRepository.findByUser_IdOrderByCreatedAtDesc(authContextService.currentUserId(), Pageable.unpaged())
                .forEach(notification -> {
                    notification.setRead(true);
                    if (notification.getReadAt() == null) {
                        notification.setReadAt(LocalDateTime.now());
                    }
                });
        return SuccessResponse.builder().success(true).build();
    }

    @Override
    @Transactional
    public void processTripStatusChangedEvent(TripStatusChangedEvent event) {
        BusinessTrip trip = businessTripService.findTrip(event.getTripId());
        Set<User> recipients = resolveRecipients(trip, event.getChangedByUserId());
        if (recipients.isEmpty()) {
            log.info("No notification recipients for trip {} status change {} -> {}",
                    trip.getId(), event.getOldStatus(), event.getNewStatus());
            return;
        }

        String title = "Статус командировки обновлён";
        String body = "Командировка #%d переведена в статус %s"
                .formatted(trip.getId(), statusLabel(event.getNewStatus()));
        String eventKey = "trip-status:%d:%s:%s"
                .formatted(trip.getId(), event.getNewStatus(), event.getChangedAt());

        Map<String, String> data = Map.of(
                "type", "trip_status_changed",
                "tripId", String.valueOf(trip.getId()),
                "oldStatus", event.getOldStatus().name(),
                "newStatus", event.getNewStatus().name(),
                "clickAction", "trip_details"
        );

        Set<Long> newlyNotifiedUserIds = new LinkedHashSet<>();
        List<UserDevice> activeDevices = devicePushTokenService.findActiveDevicesForUsers(
                recipients.stream().map(User::getId).toList());

        for (User recipient : recipients) {
            userNotificationRepository.findByUser_IdAndEventKey(recipient.getId(), eventKey)
                    .orElseGet(() -> {
                        newlyNotifiedUserIds.add(recipient.getId());
                        return createNotification(recipient, trip, event.getOldStatus(), event.getNewStatus(),
                                eventKey, title, body, data);
                    });
        }

        if (newlyNotifiedUserIds.isEmpty()) {
            log.info("Skipping duplicate push delivery for eventKey={}", eventKey);
            return;
        }

        Map<Long, Set<String>> tokensByUserId = new LinkedHashMap<>();
        for (UserDevice device : activeDevices) {
            if (newlyNotifiedUserIds.contains(device.getUser().getId())) {
                tokensByUserId.computeIfAbsent(device.getUser().getId(), key -> new LinkedHashSet<>())
                        .add(device.getPushToken());
            }
        }

        Set<String> allTokens = new LinkedHashSet<>();
        tokensByUserId.values().forEach(allTokens::addAll);
        pushDeliveryService.sendToTokens(allTokens, title, body, data);
    }

    private UserNotification createNotification(User recipient, BusinessTrip trip, BusinessTripStatus oldStatus,
                                                BusinessTripStatus newStatus, String eventKey, String title,
                                                String body, Map<String, String> data) {
        UserNotification notification = new UserNotification();
        notification.setUser(recipient);
        notification.setTrip(trip);
        notification.setType(NotificationType.TRIP_STATUS_CHANGED);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEventKey(eventKey);
        notification.setClickAction(data.get("clickAction"));
        notification.setOldStatus(oldStatus);
        notification.setNewStatus(newStatus);
        notification.setPayloadJson(toJson(data));
        return userNotificationRepository.save(notification);
    }

    private Set<User> resolveRecipients(BusinessTrip trip, Long changedByUserId) {
        Map<Long, User> recipients = new LinkedHashMap<>();
        addIfNotActor(recipients, trip.getEmployee(), changedByUserId);
        auditLogRepository.findFirstByEntityTypeAndEntityIdAndActionOrderByCreatedAtAsc("BUSINESS_TRIP", trip.getId(), "CREATED")
                .map(AuditLog::getActorUser)
                .ifPresent(user -> addIfNotActor(recipients, user, changedByUserId));
        return new LinkedHashSet<>(recipients.values());
    }

    private void addIfNotActor(Map<Long, User> recipients, User candidate, Long changedByUserId) {
        if (candidate == null || Objects.equals(candidate.getId(), changedByUserId)) {
            return;
        }
        recipients.put(candidate.getId(), candidate);
    }

    private NotificationResponse toResponse(UserNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .tripId(notification.getTrip() != null ? notification.getTrip().getId() : null)
                .clickAction(notification.getClickAction())
                .oldStatus(notification.getOldStatus())
                .newStatus(notification.getNewStatus())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String statusLabel(BusinessTripStatus status) {
        return switch (status) {
            case DRAFT -> "черновик";
            case APPROVED -> "одобрена";
            case IN_PROGRESS -> "в пути";
            case ARRIVED -> "на месте";
            case COMPLETED -> "завершена";
            case CANCELLED -> "отменена";
        };
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"" + exception.getMessage() + "\"}";
        }
    }
}
