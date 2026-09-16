package com.sample.system.ssm.service.dataaccess.mapper;

import com.sample.system.ssm.service.dataaccess.entity.command.CardSecretEntity;
import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.CardSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class CardSecretDataAccessMapper {

    public CardSecretEntity toEntity(CardSecret domain) {
        log.debug("CardSecretDataAccessMapper.toEntity started, cardSecretId={}", domain != null ? domain.cardSecretId() : null);
        if (domain == null) return null;

        var entity = new CardSecretEntity();
        if (domain.cardSecretId() != null) {
            entity.setCardSecretId(domain.cardSecretId());
        }
        entity.setCardId(domain.cardId());
        entity.setSecretType(domain.secretType().name());
        entity.setEncryptedValue(domain.encryptedValue());
        entity.setHashValue(domain.hashValue().orElse(null));
        entity.setKeyVersion(domain.keyVersion().orElse(null));
        entity.setStatus(domain.status().name());
        entity.setCreatedAt(domain.createdAt());
        entity.setRevokedAt(domain.revokedAt().orElse(null));
        return entity;
    }

    public CardSecret toDomain(CardSecretEntity entity) {
        if (entity == null) return null;

        return new CardSecret(
                entity.getCardSecretId(),
                entity.getCardId(),
                SecretType.valueOf(entity.getSecretType()),
                entity.getEncryptedValue(),
                Optional.ofNullable(entity.getHashValue()),
                Optional.ofNullable(entity.getKeyVersion()),
                CardSecretStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                Optional.ofNullable(entity.getRevokedAt())
        );
    }

    /**
     * Updates only the mutable fields on an existing entity from domain (for update-in-place).
     */
    public void updateEntityFromDomain(CardSecretEntity entity, CardSecret domain) {
        if (entity == null || domain == null) return;
        entity.setEncryptedValue(domain.encryptedValue());
        entity.setHashValue(domain.hashValue().orElse(null));
        entity.setKeyVersion(domain.keyVersion().orElse(null));
        entity.setStatus(domain.status().name());
        entity.setRevokedAt(domain.revokedAt().orElse(null));
    }
}
