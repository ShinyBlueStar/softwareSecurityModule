package com.sample.system.ssm.service.dataaccess.entity.command;

import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.enums.LockStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for managing lock states
 * Supports temporary and permanent locks
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SSM_LOCK_STATES", indexes = {
    @Index(name = "IDX_LOCK_STATES_REF", columnList = "REFERENCE_ID"),
    @Index(name = "IDX_LOCK_STATES_SCOPE", columnList = "LOCK_SCOPE"),
    @Index(name = "IDX_LOCK_STATES_STATUS", columnList = "STATUS"),
    @Index(name = "IDX_LOCK_STATES_LOCKED_UNTIL", columnList = "LOCKED_UNTIL")
})
public class LockStateEntity {

    @Id
    @Column(name = "LOCK_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lockId;

    @Enumerated(EnumType.STRING)
    @Column(name = "LOCK_SCOPE", nullable = false, length = 20)
    private LockScope lockScope; // CARD, SESSION, CHANNEL

    @Column(name = "REFERENCE_ID", nullable = false)
    private String referenceId; // PAN, session_id, or channel identifier

    @Enumerated(EnumType.STRING)
    @Column(name = "REASON", nullable = false, length = 50)
    private LockReason reason; // RETRY_LIMIT, FRAUD_SUSPECT, etc.

    @Column(name = "LOCKED_UNTIL")
    private Instant lockedUntil; // null for permanent lock

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private LockStatus status; // ACTIVE, RELEASED

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "RELEASED_AT")
    private Instant releasedAt;
}

