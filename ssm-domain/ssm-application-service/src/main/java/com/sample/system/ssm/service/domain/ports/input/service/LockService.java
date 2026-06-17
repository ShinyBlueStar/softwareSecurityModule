package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.LockState;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing lock states
 * Following Hexagonal Architecture - Input Port
 */
public interface LockService {
    
    /**
     * Check if reference is locked
     */
    boolean isLocked(LockScope scope, UUID referenceId);
    
    /**
     * Apply temporary lock
     */
    LockState applyTemporaryLock(LockScope scope, String referenceId, LockReason reason, Duration duration);
    
    /**
     * Apply permanent lock
     */
    LockState applyPermanentLock(LockScope scope, String referenceId, LockReason reason);
    
    /**
     * Release lock
     */
    LockState releaseLock(UUID lockId);
    
    /**
     * Get active locks for reference
     */
    List<LockState> getActiveLocks(UUID referenceId);
    
    /**
     * Find active lock
     */
    Optional<LockState> findActiveLock(LockScope scope, UUID referenceId);
}

