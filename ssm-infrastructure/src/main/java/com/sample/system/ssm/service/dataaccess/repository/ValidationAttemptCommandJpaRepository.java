package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.ValidationAttemptEntity;
import com.sample.system.ssm.service.domain.enums.SecretType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ValidationAttemptCommandJpaRepository extends JpaRepository<ValidationAttemptEntity, UUID> {

    @Query("SELECT COUNT(v) FROM ValidationAttemptEntity v WHERE v.referenceId = :referenceId " +
            "AND v.attemptType = :attemptType AND v.result = 'FAIL' AND v.createdAt >= :since")
    long countFailedAttempts(
            @Param("referenceId") String referenceId,
            @Param("attemptType") SecretType attemptType,
            @Param("since") Instant since
    );

    @Query("SELECT v FROM ValidationAttemptEntity v WHERE v.referenceId = :referenceId " +
            "AND v.attemptType = :attemptType ORDER BY v.createdAt DESC")
    List<ValidationAttemptEntity> findByReferenceIdAndAttemptType(
            @Param("referenceId") String referenceId,
            @Param("attemptType") SecretType attemptType
    );
}