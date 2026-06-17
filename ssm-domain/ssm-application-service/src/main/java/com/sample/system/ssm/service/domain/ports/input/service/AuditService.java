package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.enums.AuditEventType;

import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging
 * Following Hexagonal Architecture - Input Port
 */
public interface AuditService {
    
    /**
     * Log audit event
     */
    void logEvent(
        AuditEventType eventType,
        String entityType,
        UUID entityId,
        String actor,
        Map<String, Object> metadata
    );
    
    /**
     * Log audit event without metadata
     */
    default void logEvent(AuditEventType eventType, String entityType, UUID entityId, String actor) {
        logEvent(eventType, entityType, entityId, actor, null);
    }
}

