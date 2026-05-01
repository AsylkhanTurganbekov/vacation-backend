package com.company.vacation.service;

import com.company.vacation.dto.common.SuccessResponse;
import com.company.vacation.dto.device.DeactivatePushTokenRequest;
import com.company.vacation.dto.device.PushTokenRequest;
import com.company.vacation.entity.UserDevice;
import java.util.Collection;
import java.util.List;

public interface DevicePushTokenService {

    SuccessResponse registerCurrentUserToken(PushTokenRequest request);

    SuccessResponse deactivateCurrentUserToken(DeactivatePushTokenRequest request);

    List<UserDevice> findActiveDevicesForUsers(Collection<Long> userIds);

    void deactivateInvalidToken(String token);
}
