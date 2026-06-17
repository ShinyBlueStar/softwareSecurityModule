package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.enums.SecretType;

import java.util.UUID;

/**
 * Service for managing retry policies
 * Following Hexagonal Architecture - Input Port
 */
public interface RetryPolicyService {
    
    /**
     * Check if retry limit is exceeded
     */
    boolean isRetryLimitExceeded(UUID referenceId, SecretType attemptType);
    
    /**
     * Record validation attempt
     */
    void recordAttempt(UUID referenceId, SecretType attemptType, boolean success, String sourceIp);
    
    /**
     * Get retry count for reference and type
     */
    long getRetryCount(UUID referenceId, SecretType attemptType);
    
    /**
     * Reset retry count (after successful validation)
     */
    void resetRetryCount(UUID referenceId, SecretType attemptType);
}

