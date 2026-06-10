package com.sample.system.ssm.service.domain.enums;

/**
 * Types of audit events
 */
public enum AuditEventType {
    // Session events
    SESSION_CREATED,
    SESSION_INVALIDATED,
    SESSION_EXPIRED,
    
    // OTP events
    OTP_GENERATED,
    OTP_VERIFIED,
    OTP_EXPIRED,
    
    // PIN1 events
    PIN1_GENERATED,
    PIN1_VERIFIED,
    PIN1_VIEWED,
    
    // CVV2 events
    CVV2_GENERATED,
    CVV2_VERIFIED,
    CVV2_VIEWED,
    
    // Security events
    VALIDATION_ATTEMPT,
    RETRY_EXCEEDED,
    LOCK_APPLIED,
    LOCK_RELEASED,
    RATE_LIMIT_EXCEEDED
}

