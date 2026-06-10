package com.sample.system.ssm.service.domain.enums;

/**
 * Reason for applying lock
 */
public enum LockReason {
    RETRY_LIMIT_EXCEEDED,  // Maximum retry attempts exceeded
    FRAUD_SUSPECT,         // Fraudulent activity detected
    MANUAL_LOCK,           // Manually locked by admin
    SECURITY_VIOLATION      // Security policy violation
}

