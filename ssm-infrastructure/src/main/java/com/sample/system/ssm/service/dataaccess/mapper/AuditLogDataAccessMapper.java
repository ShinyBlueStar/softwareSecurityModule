package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.AuditLogEntity;
import com.sample.system.ssm.service.domain.model.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AuditLogDataAccessMapper {

    public AuditLogEntity toEntity(AuditLog domain) {
        log.info("AuditLogDataAccessMapper.toEntity domain={}", domain);
        if (domain == null) return null;

        var entity = new AuditLogEntity();
        if (domain.auditId() != null) {
            entity.setAuditId(domain.auditId());
        }
        entity.setEventType(domain.eventType());
        entity.setEntityType(domain.entityType());
        entity.setEntityId(domain.entityId().orElse(null));
        entity.setActor(domain.actor());
        entity.setEventTime(domain.eventTime());
        entity.setMetadata(domain.metadata().orElse(null));
        return entity;
    }

    public AuditLog toDomain(AuditLogEntity entity) {
        log.info("AuditLogDataAccessMapper.toDomain entity={}", entity);
        if (entity == null) return null;

        return new AuditLog(
            entity.getAuditId(),
            entity.getEventType(),
            entity.getEntityType(),
            Optional.ofNullable(entity.getEntityId()),
            entity.getActor(),
            entity.getEventTime(),
            Optional.ofNullable(entity.getMetadata())
        );
    }
}

