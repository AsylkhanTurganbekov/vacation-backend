package com.company.vacation.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushSendResult {
    private boolean configured;
    private int requestedTokens;
    private int successCount;
    private int failureCount;
}
