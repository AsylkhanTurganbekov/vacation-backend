package com.company.vacation.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushTokenResult {
    private String tokenMasked;
    private boolean success;
    private String errorCode;
    private String errorMessage;
}
