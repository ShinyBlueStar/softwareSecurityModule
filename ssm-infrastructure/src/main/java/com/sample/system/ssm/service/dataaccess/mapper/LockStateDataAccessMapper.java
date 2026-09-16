package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.LockStateEntity;
import com.sample.system.ssm.service.domain.model.LockState;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LockStateDataAccessMapper {

    public LockStateEntity toEntity(LockState domain) {
        if (domain == null) return null;

        var entity = new LockStateEntity();
        if (domain.lockId() != null) {
            entity.setLockId(domain.lockId());
        }
        entity.setLockScope(domain.lockScope());
        entity.setReferenceId(domain.referenceId());
        entity.setReason(domain.reason());
        entity.setLockedUntil(domain.lockedUntil().orElse(null));
        entity.setStatus(domain.status());
        entity.setCreatedAt(domain.createdAt());
        entity.setReleasedAt(domain.releasedAt().orElse(null));
        return entity;
    }

    public LockState toDomain(LockStateEntity entity) {
        if (entity == null) return null;

        return new LockState(
                entity.getLockId(),
                entity.getLockScope(),
                entity.getReferenceId(),
                entity.getReason(),
                Optional.ofNullable(entity.getLockedUntil()),
                entity.getStatus(),
                entity.getCreatedAt(),
                Optional.ofNullable(entity.getReleasedAt())
        );
    }
}