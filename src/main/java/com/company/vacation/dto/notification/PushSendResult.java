package com.company.vacation.dto.notification;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushSendResult {
    private boolean configured;
    private String reason;
    private String projectId;
    private int requestedTokens;
    private int successCount;
    private int failureCount;
    private List<PushTokenResult> tokenResults;
}
