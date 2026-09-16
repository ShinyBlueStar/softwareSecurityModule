package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.OtpCommandEntity;
import com.sample.system.ssm.service.domain.enums.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface OtpCommandJpaRepository extends JpaRepository<OtpCommandEntity, UUID> {
  Optional<OtpCommandEntity> findFirstByCardIdAndStatusAndExpireAtAfter(UUID cardId,
                                                                        OtpStatus status, Instant now);
  Optional<OtpCommandEntity> findFirstByCardIdOrderByExpireAtDesc(UUID cardId);

  /**
   * Bulk update: set status to EXPIRED for all OTPs where expireAt < now and status is VALID.
   * Uses a single UPDATE statement so changes are applied reliably.
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("UPDATE OtpCommandEntity e SET e.status = :expiredStatus WHERE e.expireAt < :now AND e.status = :validStatus")
  int markExpiredOtps(@Param("now") Instant now, @Param("validStatus") OtpStatus validStatus, @Param("expiredStatus") OtpStatus expiredStatus);

  /**
   * Deletes all OTPs with status in the given collection (e.g. EXPIRED, USED).
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("DELETE FROM OtpCommandEntity e WHERE e.status IN :statuses")
  int deleteByStatusIn(@Param("statuses") Collection<OtpStatus> statuses);
}
