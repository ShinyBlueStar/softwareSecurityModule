package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.mapper.LockStateDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.LockStateCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.model.LockState;
import com.sample.system.ssm.service.domain.ports.output.repository.LockStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockStateRepositoryImpl implements LockStateRepository {

    private final LockStateCommandJpaRepository jpa;
    private final LockStateDataAccessMapper mapper;

    @Override
    public LockState save(LockState lockState) {
        log.debug("LockStateRepositoryImpl.save started, lockId={}", lockState != null ? lockState.lockId() : null);
        var entity = mapper.toEntity(lockState);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<LockState> findActiveLock(LockScope scope, UUID referenceId) {
        log.debug("LockStateRepositoryImpl.findActiveLock started, scope={}, referenceId={}", scope, referenceId);
        // Convert UUID to String because referenceId in entity is String type
        return jpa.findActiveLock(scope, referenceId != null ? referenceId.toString() : null, Instant.now())
            .map(mapper::toDomain);
    }

    @Override
    public List<LockState> findActiveLocksForReference(UUID referenceId) {
        log.debug("LockStateRepositoryImpl.findActiveLocksForReference started, referenceId={}", referenceId);
        // Convert UUID to String because referenceId in entity is String type
        return jpa.findActiveLocksForReference(referenceId != null ? referenceId.toString() : null, Instant.now())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public LockState release(UUID lockId) {
        log.debug("LockStateRepositoryImpl.release started, lockId={}", lockId);
        LockState released = findById(lockId)
            .map(LockState::release)
            .orElseThrow(() -> new IllegalStateException("Lock not found: " + lockId));
        jpa.deleteById(lockId);
        return released;
    }

    @Override
    public Optional<LockState> findById(UUID lockId) {
        log.debug("LockStateRepositoryImpl.findById started, lockId={}", lockId);
        return jpa.findById(lockId).map(mapper::toDomain);
    }
}

