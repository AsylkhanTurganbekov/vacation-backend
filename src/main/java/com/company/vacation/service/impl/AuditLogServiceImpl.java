package com.company.vacation.service.impl;

import com.company.vacation.entity.AuditLog;
import com.company.vacation.entity.User;
import com.company.vacation.repository.AuditLogRepository;
import com.company.vacation.repository.UserRepository;
import com.company.vacation.service.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void log(String entityType, Long entityId, String action, Long actorUserId, Object payload) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        if (actorUserId != null) {
            User actor = userRepository.findById(actorUserId).orElse(null);
            auditLog.setActorUser(actor);
        }
        auditLog.setPayloadJson(toJson(payload));
        auditLogRepository.save(auditLog);
    }

    private String toJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"" + exception.getMessage() + "\"}";
        }
    }
}
