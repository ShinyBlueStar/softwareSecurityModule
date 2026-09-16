package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.SessionCommandEntity;
import com.sample.system.ssm.service.domain.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionCommandJpaRepository extends JpaRepository<SessionCommandEntity, UUID> {
    Optional<SessionCommandEntity> findBySessionId(UUID sessionId);

    Optional<SessionCommandEntity> findFirstByCardIdAndStatusAndExpireAtAfter(
            UUID cardId, SessionStatus status, Instant now);

    /**
     * Bulk update: set status to EXPIRED for all sessions where expireAt &lt; now and status is ACTIVE.
     * Uses a single UPDATE statement so changes are applied reliably.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE SessionCommandEntity e SET e.status = :expiredStatus WHERE e.expireAt < :now AND e.status = :activeStatus")
    int markExpiredSessions(@Param("now") Instant now, @Param("activeStatus") SessionStatus activeStatus, @Param("expiredStatus") SessionStatus expiredStatus);

    /**
     * Deletes all sessions with status in the given collection (e.g. EXPIRED, INVALIDATED).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SessionCommandEntity e WHERE e.status IN :statuses")
    int deleteByStatusIn(@Param("statuses") Collection<SessionStatus> statuses);
}
