package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.enums.ValidationResult;
import com.sample.system.ssm.service.domain.model.ValidationAttempt;
import com.sample.system.ssm.service.domain.ports.input.service.RetryPolicyService;
import com.sample.system.ssm.service.domain.ports.output.repository.ValidationAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Retry Policy Service Implementation
 * Manages retry limits and validation attempts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetryPolicyServiceImpl implements RetryPolicyService {

    private final ValidationAttemptRepository validationAttemptRepository;
    
    @Value("${ssm.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${ssm.retry.window-minutes:15}")
    private int retryWindowMinutes;

    @Override
    public boolean isRetryLimitExceeded(UUID referenceId, SecretType attemptType) {
        log.info("RetryPolicyServiceImpl.isRetryLimitExceeded started, referenceId={}, attemptType={}", referenceId, attemptType);
        String refId = referenceId.toString();
        Instant since = Instant.now().minusSeconds(retryWindowMinutes * 60L);
        long failedCount = validationAttemptRepository.countFailedAttempts(refId, attemptType, since);
        
        boolean exceeded = failedCount >= maxRetryAttempts;
        if (exceeded) {
            log.warn("Retry limit exceeded: referenceId={}, attemptType={}, failedCount={}, maxAttempts={}", 
                referenceId, attemptType, failedCount, maxRetryAttempts);
        }
        return exceeded;
    }

    @Override
    public void recordAttempt(UUID referenceId, SecretType attemptType, boolean success, String sourceIp) {
        var attempt = new ValidationAttempt(
            null,
            referenceId.toString(),
            attemptType,
            success ? ValidationResult.SUCCESS : ValidationResult.FAIL,
            Optional.ofNullable(sourceIp),
            Instant.now()
        );
        
        validationAttemptRepository.save(attempt);
        
        log.info("Recorded validation attempt: referenceId={}, attemptType={}, result={}", 
            referenceId, attemptType, attempt.result());
    }

    @Override
    public long getRetryCount(UUID referenceId, SecretType attemptType) {
        String refId = referenceId.toString();
        Instant since = Instant.now().minusSeconds(retryWindowMinutes * 60L);
        return validationAttemptRepository.countFailedAttempts(refId, attemptType, since);
    }

    @Override
    public void resetRetryCount(UUID referenceId, SecretType attemptType) {
        log.info("RetryPolicyServiceImpl.resetRetryCount started, referenceId={}, attemptType={}", referenceId, attemptType);
        // Retry count is automatically reset after time window
        // This method is for explicit reset if needed
        log.info("Retry count reset requested: referenceId={}, attemptType={}", referenceId, attemptType);
    }
}

