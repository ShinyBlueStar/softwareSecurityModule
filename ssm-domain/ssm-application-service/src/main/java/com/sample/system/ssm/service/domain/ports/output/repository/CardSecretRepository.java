package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.CardSecret;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for card secrets
 * Following Hexagonal Architecture - Output Port
 */
public interface CardSecretRepository {
    
    /**
     * Save card secret
     */
    CardSecret save(CardSecret cardSecret);
    
    /**
     * Find active secret by card ID and type
     */
    Optional<CardSecret> findActiveByCardIdAndType(UUID cardId, SecretType secretType);
    
    /**
     * Find by card secret ID
     */
    Optional<CardSecret> findById(UUID cardSecretId);
}

