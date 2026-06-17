package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.model.AuditLog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for audit logging
 * Following Hexagonal Architecture - Output Port
 */
public interface AuditLogRepository {
    
    /**
     * Save audit log entry
     */
    AuditLog save(AuditLog auditLog);
    
    /**
     * Find by entity type and ID
     */
    List<AuditLog> findByEntityTypeAndId(String entityType, UUID entityId);
    
    /**
     * Find by event type within time range
     */
    List<AuditLog> findByEventTypeAndTimeRange(AuditEventType eventType, Instant from, Instant to);
}

