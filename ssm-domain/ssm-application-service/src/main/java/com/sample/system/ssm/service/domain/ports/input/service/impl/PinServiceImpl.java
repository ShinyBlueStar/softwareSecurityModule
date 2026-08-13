package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.model.CardSecret;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.input.service.PinService;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component("PIN")
@RequiredArgsConstructor
public class PinServiceImpl implements PinService {

    private static final String CARD_DATA_DELIMITER = "\\|";

    private final VaultRepository vaultRepository;
    private final CardSecretRepository cardSecretRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public Card generate(Card card) throws SsmDomainException {
        log.debug("PinServiceImpl.generate started, cardId={}", card != null ? card.getCardId() : null);
        log.info("Generating PIN for cardId: {}", card.getCardId());
        try {
            var result = vaultRepository.generatePin(card);
            log.info("PIN generated successfully for cardId: {}", card.getCardId());

            final CardSecret cardSecret = cardSecretRepository
                    .findActiveByCardIdAndType(card.getCardId(), SecretType.PIN)
                    .map(existing -> new CardSecret(
                            existing.cardSecretId(),
                            existing.cardId(),
                            SecretType.PIN,
                            result.encryptedPin(),
                            Optional.ofNullable(result.hashPin()),
                            existing.keyVersion(),
                            CardSecretStatus.ACTIVE,
                            existing.createdAt(),
                            Optional.empty()
                    ))
                    .orElseGet(() -> new CardSecret(
                            null,
                            card.getCardId(),
                            SecretType.PIN,
                            result.encryptedPin(),
                            Optional.ofNullable(result.hashPin()),
                            Optional.empty(),
                            CardSecretStatus.ACTIVE,
                            Instant.now(),
                            Optional.empty()
                    ));
            var saved = cardSecretRepository.save(cardSecret);
            Optional.ofNullable(saved.cardSecretId())
                    .ifPresentOrElse(
                            id -> log.info("Card secret generated and stored: secretType:PIN1, cardID: {} with id: {}",card.getCardId(),
                                    id),
                            () -> log.warn("CardSecret saved but cardSecretId is null")
                    );
            auditService.logEvent(AuditEventType.PIN1_GENERATED, "CARD_SECRET", saved.cardSecretId(),
                    "SYSTEM", Map.of("cardId", card.getCardId().toString()));
            card.setEncryptedPin(result.encryptedPin());
            card.setValue(result.encryptedPin());
            return card;
        } catch (SsmDomainException e) {
            log.error("Failed to generate PIN for CardId:{}", card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating PIN", e);
            throw new SsmDomainException(
                    "PIN generation failed: " + e.getMessage(),
                    StatusService.PIN_GENERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Boolean verify(Card card) throws SsmDomainException {
        log.debug("PinServiceImpl.verify started, cardId={}", card != null ? card.getCardId() : null);
        log.info("Verifying PIN for CardId: {}", card.getCardId());
        if (card.getCardId() == null) {
            throw new SsmDomainException(
                    "CardId is required for PIN verification",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            // واکشی PIN1 رمزگذاری‌شده از جدول CardSecrets برای تطبیق با مقدار ذخیره‌شده
            CardSecret cardSecret = cardSecretRepository
                    .findActiveByCardIdAndType(card.getCardId(), SecretType.PIN)
                    .orElseThrow(() -> new SsmDomainException(
                            "Card secret not found for cardId=" + card.getCardId(),
                            StatusService.ID_NOT_FOUND,
                            HttpStatus.NOT_FOUND
                    ));
            card.setEncryptedPin(card.getValue());
            card.setHashedPin(cardSecret.hashValue().orElse(null));

            boolean isValid = vaultRepository.verifyPin(card);
            log.info("PIN verification is {} for CardId:{}", isValid ? "succeeded" : "failed", card.getCardId());
            return isValid;
        } catch (SsmDomainException e) {
            log.error("Failed to verify PIN for CardId: {}", card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying PIN", e);
            throw new SsmDomainException(
                    "PIN verification failed: " + e.getMessage(),
                    StatusService.PIN_VERIFICATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
