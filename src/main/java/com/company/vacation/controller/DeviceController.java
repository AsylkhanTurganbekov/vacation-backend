package com.company.vacation.controller;

import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.device.DeactivatePushTokenRequest;
import com.company.vacation.dto.device.PushTokenRequest;
import com.company.vacation.service.DevicePushTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DevicePushTokenService devicePushTokenService;

    @PostMapping("/push-token")
    public SuccessResponse registerPushToken(@Valid @RequestBody PushTokenRequest request) {
        return devicePushTokenService.registerCurrentUserToken(request);
    }

    @DeleteMapping("/push-token")
    public SuccessResponse deletePushToken(@Valid @RequestBody DeactivatePushTokenRequest request) {
        return devicePushTokenService.deactivateCurrentUserToken(request);
    }
}
