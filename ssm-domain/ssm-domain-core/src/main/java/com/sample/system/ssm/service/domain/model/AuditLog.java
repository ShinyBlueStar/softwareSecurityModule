package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.AuditEventType;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model for audit log entries
 */
public record AuditLog(
    UUID auditId,
    AuditEventType eventType,
    String entityType,
    Optional<UUID> entityId,
    String actor,  // User ID or "SYSTEM"
    Instant eventTime,
    Optional<String> metadata  // JSON string
) {
    public static AuditLog create(
        AuditEventType eventType,
        String entityType,
        UUID entityId,
        String actor,
        String metadata  // JSON string
    ) {
        return new AuditLog(
            null,
            eventType,
            entityType,
            Optional.ofNullable(entityId),
            actor != null ? actor : "SYSTEM",
            Instant.now(),
            Optional.ofNullable(metadata)
        );
    }
}

