package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.RateLimitService;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Rate Limit Service Implementation
 * Uses Redis for distributed rate limiting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${ssm.rate-limit.view-secret.max-requests:3}")
    private int viewSecretMaxRequests;
    @Value("${ssm.rate-limit.view-secret.window-seconds:300}")
    private int viewSecretWindowSeconds;

    @Value("${ssm.rate-limit.generate.pin.max-requests:3}")
    private int generatePinMaxRequests;
    @Value("${ssm.rate-limit.generate.otp-cvv.max-requests:5}")
    private int generateOtpCvvMaxRequests;
    @Value("${ssm.rate-limit.generate.window-seconds:300}")
    private int generateWindowSeconds;

    private static final String RATE_LIMIT_SCRIPT = """
        local key = KEYS[1]
        local maxRequests = tonumber(ARGV[1])
        local windowSeconds = tonumber(ARGV[2])
        local current = redis.call('INCR', key)
        if current == 1 then
            redis.call('EXPIRE', key, windowSeconds)
        end
        if current > maxRequests then
            return {0, current, maxRequests}
        end
        return {1, current, maxRequests}
    """;

    @Override
    public void checkRateLimit(String key, int maxRequests, int windowSeconds) throws SsmDomainException {
        log.debug("RateLimitServiceImpl.checkRateLimit started, key={}", key);
        String redisKey = "rate_limit:" + key;
        
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(RATE_LIMIT_SCRIPT);
        script.setResultType(List.class);
        
        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
            script,
            Collections.singletonList(redisKey),
            String.valueOf(maxRequests),
            String.valueOf(windowSeconds)
        );
        
        if (result != null && result.size() >= 3) {
            long allowed = result.get(0);
            long current = result.get(1);
            long max = result.get(2);
            
            if (allowed == 0) {
                log.warn("Rate limit exceeded: key={}, current={}, max={}", key, current, max);
                throw new SsmDomainException(
                        "Rate limit exceeded: %d requests allowed per %d seconds".formatted(max, windowSeconds),
                        StatusService.RATE_LIMIT_EXCEEDED,
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }
            
            log.debug("Rate limit check passed: key={}, current={}, max={}", key, current, max);
        }
    }

    @Override
    public void checkViewSecretRateLimit(UUID cardId, String secretType) throws SsmDomainException {
        log.debug("RateLimitServiceImpl.checkViewSecretRateLimit started, cardId={}, secretType={}", cardId, secretType);
        String key = "view_%s:%s".formatted(secretType.toLowerCase(), cardId);
        checkRateLimit(key, viewSecretMaxRequests, viewSecretWindowSeconds);
    }

    @Override
    public void checkGenerateRateLimit(UUID cardId, String type) throws SsmDomainException {
        String key = "generate_%s:%s".formatted(type.toUpperCase(), cardId);
        int maxRequests = "PIN".equalsIgnoreCase(type) ? generatePinMaxRequests : generateOtpCvvMaxRequests;
        checkRateLimit(key, maxRequests, generateWindowSeconds);
    }
}

