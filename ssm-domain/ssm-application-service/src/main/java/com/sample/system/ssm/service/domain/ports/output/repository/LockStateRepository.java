package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.model.LockState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for lock states
 * Following Hexagonal Architecture - Output Port
 */
public interface LockStateRepository {
    
    /**
     * Save lock state
     */
    LockState save(LockState lockState);
    
    /**
     * Find active lock by scope and reference ID
     */
    Optional<LockState> findActiveLock(LockScope scope, UUID referenceId);
    
    /**
     * Find all active locks for a reference ID (across all scopes)
     */
    List<LockState> findActiveLocksForReference(UUID referenceId);
    
    /**
     * Release lock
     */
    LockState release(UUID lockId);
    
    /**
     * Find by lock ID
     */
    Optional<LockState> findById(UUID lockId);
}

