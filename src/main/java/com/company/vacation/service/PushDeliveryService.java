package com.company.vacation.service;

import com.company.vacation.dto.notification.PushSendResult;
import java.util.Collection;
import java.util.Map;

public interface PushDeliveryService {

    PushSendResult sendToTokens(Collection<String> tokens, String title, String body, Map<String, String> data);
}
