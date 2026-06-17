package com.sample.system.ssm.service.domain.response;

/**
 * Response for viewing card secret
 */
public record ViewCardSecretResponse(
    String encryptedSecret  // Encrypted value - Card Module will decrypt
) {}

