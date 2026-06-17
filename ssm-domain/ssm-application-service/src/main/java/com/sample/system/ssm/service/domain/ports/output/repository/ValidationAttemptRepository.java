package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.ValidationAttempt;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for validation attempts
 * Following Hexagonal Architecture - Output Port
 */
public interface ValidationAttemptRepository {
    
    /**
     * Save validation attempt
     */
    ValidationAttempt save(ValidationAttempt attempt);
    
    /**
     * Count failed attempts for reference ID and type within time window
     * @param referenceId PAN or sessionId (String)
     */
    long countFailedAttempts(String referenceId, SecretType attemptType, Instant since);
    
    /**
     * Get all attempts for reference ID and type
     * @param referenceId PAN or sessionId (String)
     */
    List<ValidationAttempt> findByReferenceIdAndType(String referenceId, SecretType attemptType);
}

