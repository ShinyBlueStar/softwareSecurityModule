package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.mapper.CardSecretDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.CardSecretCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.CardSecret;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSecretRepositoryImpl implements CardSecretRepository {

    private final CardSecretCommandJpaRepository jpa;
    private final CardSecretDataAccessMapper mapper;

    @Override
    @Transactional
    public CardSecret save(CardSecret cardSecret) {
        log.debug("CardSecretRepositoryImpl.save started, cardSecretId={}", cardSecret != null ? cardSecret.cardSecretId() : null);
        if (cardSecret.cardSecretId() != null) {
            // For existing entities, load and update the managed entity to avoid merge conflicts
            // This ensures we're working with the entity from the persistence context
            var existingOpt = jpa.findById(cardSecret.cardSecretId());
            if (existingOpt.isPresent()) {
                var existing = existingOpt.get();
                // Update the managed entity in-place - this is safe and avoids merge issues
                mapper.updateEntityFromDomain(existing, cardSecret);
                // Save will flush changes - entity is already managed
                var saved = jpa.save(existing);
                return mapper.toDomain(saved);
            }
            // Entity with this ID doesn't exist (shouldn't happen normally, but handle gracefully)
            log.debug("Entity not found with ID {}, creating new entity", cardSecret.cardSecretId());
        }
        // New entity without ID or entity not found
        var entity = mapper.toEntity(cardSecret);
        var saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CardSecret> findActiveByCardIdAndType(UUID cardId, SecretType secretType) {
        log.debug("CardSecretRepositoryImpl.findActiveByCardIdAndType started, cardId={}, secretType={}", cardId, secretType);
        return jpa.findByCardIdAndSecretTypeAndStatus(
                cardId,
                secretType.name(),
                CardSecretStatus.ACTIVE.name()
        ).map(mapper::toDomain);
    }

    @Override
    public Optional<CardSecret> findById(UUID cardSecretId) {
        log.debug("CardSecretRepositoryImpl.findById started, cardSecretId={}", cardSecretId);
        return jpa.findById(cardSecretId).map(mapper::toDomain);
    }
}

