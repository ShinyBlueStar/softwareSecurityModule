package com.sample.system.ssm.service.dataaccess.entity.command;

import com.sample.system.ssm.service.domain.enums.AuditEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for audit logging
 * Stores all security-related events with metadata
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SSM_AUDIT_LOG", indexes = {
    @Index(name = "IDX_AUDIT_LOG_EVENT_TYPE", columnList = "EVENT_TYPE"),
    @Index(name = "IDX_AUDIT_LOG_ENTITY", columnList = "ENTITY_TYPE, ENTITY_ID"),
    @Index(name = "IDX_AUDIT_LOG_TIME", columnList = "EVENT_TIME")
})
public class AuditLogEntity {

    @Id
    @Column(name = "AUDIT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID auditId;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(name = "ENTITY_TYPE", nullable = false, length = 50)
    private String entityType; // SESSION, OTP, CARD_SECRET, etc.

    @Column(name = "ENTITY_ID")
    private UUID entityId;

    @Column(name = "ACTOR", length = 100)
    private String actor; // User or system identifier

    @Column(name = "EVENT_TIME", nullable = false)
    private Instant eventTime;

    @Column(name = "METADATA", columnDefinition = "CLOB")
    private String metadata; // JSON string for additional event data (transaction context, etc.)
}

