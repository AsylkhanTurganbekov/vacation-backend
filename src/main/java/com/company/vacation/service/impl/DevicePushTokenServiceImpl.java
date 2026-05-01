package com.company.vacation.service.impl;

import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.device.DeactivatePushTokenRequest;
import com.company.vacation.dto.device.PushTokenRequest;
import com.company.vacation.entity.User;
import com.company.vacation.entity.UserDevice;
import com.company.vacation.exception.ApiValidationException;
import com.company.vacation.repository.UserDeviceRepository;
import com.company.vacation.service.AuditLogService;
import com.company.vacation.service.AuthContextService;
import com.company.vacation.service.DevicePushTokenService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevicePushTokenServiceImpl implements DevicePushTokenService {

    private final UserDeviceRepository userDeviceRepository;
    private final AuthContextService authContextService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SuccessResponse registerCurrentUserToken(PushTokenRequest request) {
        User currentUser = authContextService.currentUser();
        UserDevice device = userDeviceRepository.findByPushToken(request.getToken())
                .orElseGet(() -> userDeviceRepository.findByUser_IdAndDeviceId(currentUser.getId(), request.getDeviceId())
                        .orElseGet(UserDevice::new));

        device.setUser(currentUser);
        device.setPushToken(request.getToken());
        device.setPlatform(request.getPlatform());
        device.setDeviceId(request.getDeviceId());
        device.setDeviceName(request.getDeviceName());
        device.setAppVersion(request.getAppVersion());
        device.setActive(true);
        device.setLastSeenAt(LocalDateTime.now());
        userDeviceRepository.save(device);

        auditLogService.log("USER_DEVICE", device.getId(), "REGISTERED", currentUser.getId(), request);
        return SuccessResponse.builder().success(true).build();
    }

    @Override
    @Transactional
    public SuccessResponse deactivateCurrentUserToken(DeactivatePushTokenRequest request) {
        if ((request.getToken() == null || request.getToken().isBlank())
                && (request.getDeviceId() == null || request.getDeviceId().isBlank())) {
            throw new ApiValidationException("Either token or deviceId must be provided");
        }

        Long currentUserId = authContextService.currentUserId();
        userDeviceRepository.findByUser_IdAndPushToken(currentUserId, request.getToken())
                .or(() -> userDeviceRepository.findByUser_IdAndDeviceId(currentUserId, request.getDeviceId()))
                .ifPresent(device -> {
                    device.setActive(false);
                    device.setLastSeenAt(LocalDateTime.now());
                    auditLogService.log("USER_DEVICE", device.getId(), "DEACTIVATED", currentUserId, request);
                });

        return SuccessResponse.builder().success(true).build();
    }

    @Override
    public List<UserDevice> findActiveDevicesForUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userDeviceRepository.findByUser_IdInAndActiveTrue(userIds);
    }

    @Override
    @Transactional
    public void deactivateInvalidToken(String token) {
        userDeviceRepository.findByPushToken(token).ifPresent(device -> {
            device.setActive(false);
            device.setLastSeenAt(LocalDateTime.now());
            auditLogService.log("USER_DEVICE", device.getId(), "DEACTIVATED_INVALID_TOKEN", null, token);
        });
    }
}
