package com.company.vacation.dto.notification;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestPushResponse {
    private boolean success;
    private int devicesFound;
    private boolean configured;
    private String reason;
    private String projectId;
    private int successCount;
    private int failureCount;
    private List<PushTokenResult> tokenResults;
}
