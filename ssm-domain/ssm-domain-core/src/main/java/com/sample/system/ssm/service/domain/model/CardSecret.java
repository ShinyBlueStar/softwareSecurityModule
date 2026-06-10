package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model for card secrets (PIN1, CVV2)
 * Only encrypted values are stored - no plain values
 */
public record CardSecret(
    UUID cardSecretId,
    UUID cardId,
    SecretType secretType,
    String encryptedValue,  // Encrypted from vault-service
    Optional<String> hashValue,  // HMAC hash for PIN1
    Optional<String> keyVersion,
    CardSecretStatus status,
    Instant createdAt,
    Optional<Instant> revokedAt
) {
    public boolean isActive() {
        return status == CardSecretStatus.ACTIVE;
    }

    public CardSecret revoke() {
        return new CardSecret(
            cardSecretId,
            cardId,
            secretType,
            encryptedValue,
            hashValue,
            keyVersion,
            CardSecretStatus.REVOKED,
            createdAt,
            Optional.of(Instant.now())
        );
    }
}

