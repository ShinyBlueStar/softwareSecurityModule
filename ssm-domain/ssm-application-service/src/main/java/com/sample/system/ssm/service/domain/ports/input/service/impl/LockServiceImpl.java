package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.enums.LockStatus;
import com.sample.system.ssm.service.domain.model.LockState;
import com.sample.system.ssm.service.domain.ports.input.service.LockService;
import com.sample.system.ssm.service.domain.ports.output.repository.LockStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Lock Service Implementation
 * Manages temporary and permanent locks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LockServiceImpl implements LockService {

    private final LockStateRepository lockStateRepository;
    
    @Value("${ssm.lock.default-duration-minutes:30}")
    private int defaultLockDurationMinutes;

    @Override
    public boolean isLocked(LockScope scope, UUID referenceId) {
        log.debug("LockServiceImpl.isLocked started, scope={}, referenceId={}", scope, referenceId);
        return lockStateRepository.findActiveLock(scope, referenceId)
            .filter(LockState::isActive)
            .filter(lock -> !lock.isExpired(Instant.now()))
            .isPresent();
    }

    @Override
    public LockState applyTemporaryLock(LockScope scope, String referenceId, LockReason reason, Duration duration) {
        log.info("Applying temporary lock: scope={}, referenceId={}, reason={}, duration={}", 
            scope, referenceId, reason, duration);
        
        return Optional.ofNullable(referenceId)
            .map(refId -> createLock(scope, refId, reason, Optional.of(Instant.now().plus(duration))))
            .orElseThrow(() -> new IllegalArgumentException("Reference ID cannot be null"));
    }

    @Override
    public LockState applyPermanentLock(LockScope scope, String referenceId, LockReason reason) {
        log.info("Applying permanent lock: scope={}, referenceId={}, reason={}", 
            scope, referenceId, reason);
        
        return Optional.ofNullable(referenceId)
            .map(refId -> createLock(scope, refId, reason, Optional.empty()))
            .orElseThrow(() -> new IllegalArgumentException("Reference ID cannot be null"));
    }

    @Override
    public LockState releaseLock(UUID lockId) {
        log.info("Releasing lock: lockId={}", lockId);
        return lockStateRepository.release(lockId);
    }

    @Override
    public List<LockState> getActiveLocks(UUID referenceId) {
        log.debug("LockServiceImpl.getActiveLocks started, referenceId={}", referenceId);
        return lockStateRepository.findActiveLocksForReference(referenceId)
            .stream()
            .filter(lock -> !lock.isExpired(Instant.now()))
            .toList();
    }

    @Override
    public Optional<LockState> findActiveLock(LockScope scope, UUID referenceId) {
        log.debug("LockServiceImpl.findActiveLock started, scope={}, referenceId={}", scope, referenceId);
        return lockStateRepository.findActiveLock(scope, referenceId)
            .filter(lock -> !lock.isExpired(Instant.now()));
    }

    private LockState createLock(LockScope scope, String referenceId, LockReason reason, Optional<Instant> lockedUntil) {
        log.debug("LockServiceImpl.createLock started, scope={}, referenceId={}", scope, referenceId);
        var lockState = new LockState(
            null,
            scope,
            referenceId,
            reason,
            lockedUntil,
            LockStatus.ACTIVE,
            Instant.now(),
            Optional.empty()
        );
        return lockStateRepository.save(lockState);
    }
}

