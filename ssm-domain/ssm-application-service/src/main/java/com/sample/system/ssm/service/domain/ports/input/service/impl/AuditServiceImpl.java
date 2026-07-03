package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.model.AuditLog;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.output.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Audit Service Implementation
 * Logs all security-related events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logEvent(
        AuditEventType eventType,
        String entityType,
        UUID entityId,
        String actor,
        Map<String, Object> metadata
    ) {
        log.debug("AuditServiceImpl.logEvent started, eventType={}, entityType={}, entityId={}", eventType, entityType, entityId);
        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize metadata to JSON: {}", e.getMessage());
            }
        }
        
        var auditLog = AuditLog.create(eventType, entityType, entityId, actor, metadataJson);
        auditLogRepository.save(auditLog);
        
        log.info("Audit event logged: eventType={}, entityType={}, entityId={}, actor={}", 
            eventType, entityType, entityId, actor);
    }
}

