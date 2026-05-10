package com.company.vacation.service.impl;

import com.company.vacation.dto.common.PagedResponse;
import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.notification.NotificationResponse;
import com.company.vacation.dto.notification.PushSendResult;
import com.company.vacation.dto.notification.TestPushRequest;
import com.company.vacation.dto.notification.TestPushResponse;
import com.company.vacation.entity.AuditLog;
import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.User;
import com.company.vacation.entity.UserDevice;
import com.company.vacation.entity.UserNotification;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.NotificationType;
import com.company.vacation.entity.enums.TripEventType;
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
    @Transactional(readOnly = true)
    public TestPushResponse sendTestPush(TestPushRequest request) {
        Long currentUserId = authContextService.currentUserId();
        List<UserDevice> activeDevices = devicePushTokenService.findActiveDevicesForUsers(List.of(currentUserId));
        Set<String> tokens = activeDevices.stream()
                .map(UserDevice::getPushToken)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        log.info("Test push requested: userId={}, tripId={}, devicesFound={}, tokenMasks={}",
                currentUserId,
                request.getTripId(),
                tokens.size(),
                tokens.stream().map(this::maskToken).toList());

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "test_push");
        data.put("clickAction", "notification_test");
        if (request.getTripId() != null) {
            data.put("tripId", String.valueOf(request.getTripId()));
        }

        PushSendResult sendResult = pushDeliveryService.sendToTokens(tokens, request.getTitle(), request.getBody(), data);
        log.info("Test push result: userId={}, tripId={}, devicesFound={}, configured={}, reason={}, projectId={}, successCount={}, failureCount={}",
                currentUserId,
                request.getTripId(),
                tokens.size(),
                sendResult.isConfigured(),
                sendResult.getReason(),
                sendResult.getProjectId(),
                sendResult.getSuccessCount(),
                sendResult.getFailureCount());
        return TestPushResponse.builder()
                .success(sendResult.isConfigured() && sendResult.getSuccessCount() > 0)
                .devicesFound(tokens.size())
                .configured(sendResult.isConfigured())
                .reason(sendResult.getReason())
                .projectId(sendResult.getProjectId())
                .successCount(sendResult.getSuccessCount())
                .failureCount(sendResult.getFailureCount())
                .tokenResults(sendResult.getTokenResults())
                .build();
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
        String body = "%s — %s — статус: %s"
                .formatted(trip.getEmployee().getFullName(), tripPurpose(trip), statusLabel(event.getNewStatus()));
        String eventKey = "trip-status:%d:%s:%s"
                .formatted(trip.getId(), event.getNewStatus(), event.getChangedAt());

        Map<String, Object> payload = buildTripStatusPayload(trip, event);
        Map<String, String> data = toFcmDataPayload(payload);

        Set<Long> newlyNotifiedUserIds = new LinkedHashSet<>();
        List<UserDevice> activeDevices = devicePushTokenService.findActiveDevicesForUsers(
                recipients.stream().map(User::getId).toList());

        for (User recipient : recipients) {
            userNotificationRepository.findByUser_IdAndEventKey(recipient.getId(), eventKey)
                    .orElseGet(() -> {
                        newlyNotifiedUserIds.add(recipient.getId());
                        return createNotification(recipient, trip, event.getOldStatus(), event.getNewStatus(),
                                eventKey, title, body, payload);
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
                                                String body, Map<String, Object> payload) {
        UserNotification notification = new UserNotification();
        notification.setUser(recipient);
        notification.setTrip(trip);
        notification.setType(NotificationType.TRIP_STATUS_CHANGED);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEventKey(eventKey);
        notification.setClickAction((String) payload.get("clickAction"));
        notification.setOldStatus(oldStatus);
        notification.setNewStatus(newStatus);
        notification.setPayloadJson(toJson(payload));
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
        Map<String, Object> payload = parsePayload(notification.getPayloadJson());
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .tripId(notification.getTrip() != null ? notification.getTrip().getId() : null)
                .clickAction(notification.getClickAction())
                .oldStatus(notification.getOldStatus())
                .newStatus(notification.getNewStatus())
                .employeeId(asLong(payload.get("employeeId")))
                .employeeName((String) payload.get("employeeName"))
                .tripPurpose((String) payload.get("tripPurpose"))
                .destinationAddress((String) payload.get("destinationAddress"))
                .eventType(asTripEventType(payload.get("eventType")))
                .eventTime(asLocalDateTime(payload.get("eventTime")))
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private Map<String, Object> buildTripStatusPayload(BusinessTrip trip, TripStatusChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "trip_status_changed");
        payload.put("tripId", trip.getId());
        payload.put("employeeId", trip.getEmployee().getId());
        payload.put("employeeName", trip.getEmployee().getFullName());
        payload.put("tripPurpose", tripPurpose(trip));
        payload.put("destinationAddress", trip.getDestinationAddress());
        payload.put("oldStatus", event.getOldStatus() != null ? event.getOldStatus().name() : null);
        payload.put("newStatus", event.getNewStatus() != null ? event.getNewStatus().name() : null);
        payload.put("eventType", event.getEventType() != null ? event.getEventType().name() : null);
        payload.put("eventTime", event.getEventTime() != null ? event.getEventTime().toString() : null);
        payload.put("clickAction", "trip_details");
        return payload;
    }

    private Map<String, String> toFcmDataPayload(Map<String, Object> payload) {
        Map<String, String> data = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (value != null) {
                data.put(key, String.valueOf(value));
            }
        });
        return data;
    }

    private String tripPurpose(BusinessTrip trip) {
        if (trip.getPurpose() != null && !trip.getPurpose().isBlank()) {
            return trip.getPurpose().trim();
        }
        if (trip.getDestinationAddress() != null && !trip.getDestinationAddress().isBlank()) {
            return trip.getDestinationAddress().trim();
        }
        return "Командировка";
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

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"" + exception.getMessage() + "\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payloadJson, LinkedHashMap.class);
        } catch (Exception exception) {
            log.warn("Failed to parse notification payload JSON: {}", exception.getMessage());
            return Map.of();
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value));
        } catch (Exception exception) {
            return null;
        }
    }

    private TripEventType asTripEventType(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return TripEventType.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        if (token.length() <= 12) {
            return token.substring(0, Math.min(4, token.length())) + "***";
        }
        return token.substring(0, 6) + "***" + token.substring(token.length() - 6);
    }
}
