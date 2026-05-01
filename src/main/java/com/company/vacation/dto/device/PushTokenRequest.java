package com.company.vacation.dto.device;

import com.company.vacation.entity.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushTokenRequest {

    @NotBlank
    @Size(max = 2048)
    private String token;

    @NotNull
    private DevicePlatform platform;

    @NotBlank
    @Size(max = 255)
    private String deviceId;

    @Size(max = 255)
    private String deviceName;

    @Size(max = 50)
    private String appVersion;
}
