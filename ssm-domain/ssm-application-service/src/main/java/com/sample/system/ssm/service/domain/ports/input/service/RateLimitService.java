package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;

import java.util.UUID;

/**
 * Service for rate limiting
 * Following Hexagonal Architecture - Input Port
 */
public interface RateLimitService {
    void checkRateLimit(String key, int maxRequests, int windowSeconds) throws SsmDomainException;
    void checkViewSecretRateLimit(UUID cardId, String secretType) throws SsmDomainException;
    void checkGenerateRateLimit(UUID actualCardId, String upperReqType) throws SsmDomainException;
}

