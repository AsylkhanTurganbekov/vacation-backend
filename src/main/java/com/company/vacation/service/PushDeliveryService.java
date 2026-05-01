package com.company.vacation.service;

import java.util.Collection;
import java.util.Map;

public interface PushDeliveryService {

    void sendToTokens(Collection<String> tokens, String title, String body, Map<String, String> data);
}
