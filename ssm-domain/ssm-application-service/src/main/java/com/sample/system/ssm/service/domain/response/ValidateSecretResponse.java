package com.sample.system.ssm.service.domain.response;

/**
 * Response for secret validation
 */
public record ValidateSecretResponse(
    boolean valid,
    String reason  // SUCCESS, INVALID_SECRET, RETRY_EXCEEDED, etc.
) {}

