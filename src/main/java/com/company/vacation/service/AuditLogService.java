package com.company.vacation.service;

public interface AuditLogService {

    void log(String entityType, Long entityId, String action, Long actorUserId, Object payload);
}
