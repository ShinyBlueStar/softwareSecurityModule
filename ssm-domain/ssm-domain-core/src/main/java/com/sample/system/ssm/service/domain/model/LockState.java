package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.enums.LockStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model for lock states
 */
public record LockState(
    UUID lockId,
    LockScope lockScope,
    String referenceId,  // PAN, sessionId, or channel identifier
    LockReason reason,
    Optional<Instant> lockedUntil,  // null for permanent lock
    LockStatus status,
    Instant createdAt,
    Optional<Instant> releasedAt
) {
    public boolean isActive() {
        return status == LockStatus.ACTIVE;
    }

    public boolean isTemporary() {
        return lockedUntil.isPresent();
    }

    public boolean isExpired(Instant now) {
        return lockedUntil.map(until -> now.isAfter(until)).orElse(false);
    }

    public LockState release() {
        return new LockState(
            lockId,
            lockScope,
            referenceId,
            reason,
            lockedUntil,
            LockStatus.RELEASED,
            createdAt,
            Optional.of(Instant.now())
        );
    }
}

