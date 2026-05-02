package com.company.vacation.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestPushResponse {
    private boolean success;
    private int devicesFound;
    private boolean configured;
    private int successCount;
    private int failureCount;
}
