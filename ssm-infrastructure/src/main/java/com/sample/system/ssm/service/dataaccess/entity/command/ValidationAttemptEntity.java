package com.sample.system.ssm.service.dataaccess.entity.command;

import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.enums.ValidationResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity for tracking validation attempts
 * Used for retry limit management and audit purposes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SSM_VALIDATION_ATTEMPTS", indexes = {
    @Index(name = "IDX_VALIDATION_ATTEMPTS_REF", columnList = "REFERENCE_ID"),
    @Index(name = "IDX_VALIDATION_ATTEMPTS_TYPE", columnList = "ATTEMPT_TYPE"),
    @Index(name = "IDX_VALIDATION_ATTEMPTS_CREATED", columnList = "CREATED_AT")
})
public class ValidationAttemptEntity {

    @Id
    @Column(name = "ATTEMPT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID attemptId;

    @Column(name = "REFERENCE_ID", nullable = false)
    private String referenceId; // PAN or session_id

    @Enumerated(EnumType.STRING)
    @Column(name = "ATTEMPT_TYPE", nullable = false, length = 10)
    private SecretType attemptType; // PIN1, PIN2, CVV2

    @Enumerated(EnumType.STRING)
    @Column(name = "RESULT", nullable = false, length = 10)
    private ValidationResult result; // SUCCESS, FAIL

    @Column(name = "SOURCE_IP", length = 15)
    private String sourceIp;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;
}

