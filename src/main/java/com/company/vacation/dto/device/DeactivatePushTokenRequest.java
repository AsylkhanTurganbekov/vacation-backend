package com.company.vacation.dto.device;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivatePushTokenRequest {

    @Size(max = 2048)
    private String token;

    @Size(max = 255)
    private String deviceId;
}
