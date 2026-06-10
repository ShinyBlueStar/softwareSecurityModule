package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.enums.ValidationResult;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model for validation attempts
 */
public record ValidationAttempt(
    UUID attemptId,
    String referenceId,  // PAN or sessionId
    SecretType attemptType,
    ValidationResult result,
    Optional<String> sourceIp,
    Instant createdAt
) {
    public boolean isSuccess() {
        return result == ValidationResult.SUCCESS;
    }

    public boolean isFailure() {
        return result == ValidationResult.FAIL;
    }
}

