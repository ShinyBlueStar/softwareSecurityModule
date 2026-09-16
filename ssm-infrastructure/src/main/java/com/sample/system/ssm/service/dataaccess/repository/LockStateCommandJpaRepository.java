package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.LockStateEntity;
import com.sample.system.ssm.service.domain.enums.LockScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LockStateCommandJpaRepository extends JpaRepository<LockStateEntity, UUID> {

    @Query("SELECT l FROM LockStateEntity l WHERE l.lockScope = :scope " +
           "AND l.referenceId = :referenceId AND l.status = 'ACTIVE' " +
           "AND (l.lockedUntil IS NULL OR l.lockedUntil > :now)")
    Optional<LockStateEntity> findActiveLock(
        @Param("scope") LockScope scope,
        @Param("referenceId") String referenceId,
        @Param("now") Instant now
    );
    
    @Query("SELECT l FROM LockStateEntity l WHERE l.referenceId = :referenceId " +
           "AND l.status = 'ACTIVE' AND (l.lockedUntil IS NULL OR l.lockedUntil > :now)")
    List<LockStateEntity> findActiveLocksForReference(
        @Param("referenceId") String referenceId,
        @Param("now") Instant now
    );
    
    @Modifying
    @Query("UPDATE LockStateEntity l SET l.status = 'RELEASED', l.releasedAt = :releasedAt " +
           "WHERE l.lockId = :lockId")
    void releaseLock(@Param("lockId") UUID lockId, @Param("releasedAt") Instant releasedAt);
}

