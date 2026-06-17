package com.sample.system.ssm.service.domain.response;

import java.util.UUID;

/**
 * Response for card secret generation
 */
public record GenerateCardSecretResponse(
    UUID cardSecretId,
    String encryptedSecret,  // Encrypted value to be sent to Card Module
    String keyVersion  // Optional key version
) {}

