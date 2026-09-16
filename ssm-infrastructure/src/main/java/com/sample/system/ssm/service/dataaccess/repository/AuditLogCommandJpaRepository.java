package com.sample.system.ssm.service.dataaccess.repository;

import com.sample.system.ssm.service.dataaccess.entity.command.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditLogCommandJpaRepository extends JpaRepository<AuditLogEntity, UUID> {
    
    @Query("SELECT a FROM AuditLogEntity a WHERE a.entityType = :entityType " +
           "AND a.entityId = :entityId ORDER BY a.eventTime DESC")
    List<AuditLogEntity> findByEntityTypeAndEntityId(
        @Param("entityType") String entityType,
        @Param("entityId") UUID entityId
    );
    
    @Query("SELECT a FROM AuditLogEntity a WHERE a.eventType = :eventType " +
           "AND a.eventTime >= :from AND a.eventTime <= :to ORDER BY a.eventTime DESC")
    List<AuditLogEntity> findByEventTypeAndTimeRange(
        @Param("eventType") String eventType,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}

