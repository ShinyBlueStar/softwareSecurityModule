package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.mapper.AuditLogDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.AuditLogCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.model.AuditLog;
import com.sample.system.ssm.service.domain.ports.output.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogCommandJpaRepository jpa;
    private final AuditLogDataAccessMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        log.debug("AuditLogRepositoryImpl.save started, entityType={}, entityId={}", auditLog != null ? auditLog.entityType() : null,
                auditLog != null ? auditLog.entityId() : null);
        var entity = mapper.toEntity(auditLog);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findByEntityTypeAndId(String entityType, UUID entityId) {
        return jpa.findByEntityTypeAndEntityId(entityType, entityId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<AuditLog> findByEventTypeAndTimeRange(AuditEventType eventType, Instant from, Instant to) {
        log.debug("AuditLogRepositoryImpl.findByEventTypeAndTimeRange started, eventType={}, from={}, to={}", eventType, from, to);
        return jpa.findByEventTypeAndTimeRange(eventType.name(), from, to)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}

