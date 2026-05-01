package com.company.vacation.repository;

import com.company.vacation.entity.AuditLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "actorUser")
    Optional<AuditLog> findFirstByEntityTypeAndEntityIdAndActionOrderByCreatedAtAsc(
            String entityType, Long entityId, String action);
}
