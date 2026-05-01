package com.company.vacation.service.impl;

import com.company.vacation.event.TripStatusChangedEvent;
import com.company.vacation.service.NotificationCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TripStatusChangedNotificationListener {

    private final NotificationCenterService notificationCenterService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTripStatusChanged(TripStatusChangedEvent event) {
        notificationCenterService.processTripStatusChangedEvent(event);
    }
}
