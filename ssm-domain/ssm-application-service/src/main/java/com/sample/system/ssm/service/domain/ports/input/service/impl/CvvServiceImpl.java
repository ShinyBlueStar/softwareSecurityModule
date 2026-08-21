package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.model.CardSecret;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.input.service.CvvService;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component("CVV")
@RequiredArgsConstructor
public class CvvServiceImpl implements CvvService {

    private final VaultRepository vaultRepository;
    private final AuditService auditService;
    private final CardSecretRepository cardSecretRepository;

    @Override
    public Card generate(Card card) throws SsmDomainException {
        log.info("CVV2 generated: CardId: {}", card.getCardId());

        if (card.getEncryptedData() == null || card.getEncryptedData().isBlank()) {
            throw new SsmDomainException(
                    "encryptedData is required for CVV generation",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            VaultRepository.CvvGenerationResult result = vaultRepository.generateCvv2(card);
            card.setValue(result.encryptedCvv());
            card.setEncryptedCvv(result.encryptedCvv());

            // ذخیره رکورد CVV2 در دیتابیس برای استفاده در verify
            CardSecret cardSecret = cardSecretRepository
                    .findActiveByCardIdAndType(card.getCardId(), SecretType.CVV)
                    .map(existing -> new CardSecret(
                            existing.cardSecretId(),
                            existing.cardId(),
                            SecretType.CVV,
                            result.encryptedCvv(),
                            Optional.ofNullable(result.hashCvv()),
                            existing.keyVersion(),
                            CardSecretStatus.ACTIVE,
                            existing.createdAt(),
                            Optional.empty()
                    ))
                    .orElseGet(() -> new CardSecret(
                            null,
                            card.getCardId(),
                            SecretType.CVV,
                            result.encryptedCvv(),
                            Optional.ofNullable(result.hashCvv()),
                            Optional.empty(),
                            CardSecretStatus.ACTIVE,
                            Instant.now(),
                            Optional.empty()
                    ));
            cardSecretRepository.save(cardSecret);
            log.debug("CVV2 secret saved to DB for cardId: {}", card.getCardId());

            auditService.logEvent(AuditEventType.CVV2_GENERATED, "CARD", card.getCardId(),
                    "SYSTEM", Map.of("cardId", card.getCardId().toString()));
            log.info("CVV2 generated successfully for CardId: {}", card.getCardId());
            return card;
        } catch (SsmDomainException e) {
            log.error("Failed to generate CVV2 for CardId: {}", card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating CVV2", e);
            throw new SsmDomainException(
                    "CVV2 generation failed: " + e.getMessage(),
                    StatusService.CVV2_GENERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Boolean verify(Card card) throws SsmDomainException {
        log.info("Verifying CVV2 for CardId: {}", card.getCardId());
        String pan = card.getPan();
        String expTime = card.getExpTime();
        String serviceCode = card.getServiceCode();

        var cardSecretOpt = cardSecretRepository.findActiveByCardIdAndType(card.getCardId(), SecretType.CVV);
        if (cardSecretOpt.isEmpty()) {
            log.warn("CVV2 secret not found in DB for cardId: {}", card.getCardId());
            throw new SsmDomainException(
                    "CVV2 not generated for this card",
                    StatusService.REQUESTED_CVV2_NOT_EXISTED,
                    HttpStatus.BAD_REQUEST
            );
        }
        var cardSecret = cardSecretOpt.get();
        card.setHashedCvv(cardSecret.hashValue().orElse(null));
        log.info("CVV2 secret loaded from DB for cardId: {}", card.getCardId());

        try {
            boolean isValid = vaultRepository.verifyCvv2(card);
            log.info("CVV2 verification {} for cardId: {}", isValid ? "succeeded" : "failed",
                    card.getCardId());
            return isValid;
        } catch (SsmDomainException e) {
            log.error("Failed to verify CVV2 for cardId: {}",card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying CVV2", e);
            throw new SsmDomainException(
                    "CVV2 verification failed: " + e.getMessage(),
                    StatusService.CVV2_VERIFICATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private String getLastFourDigits(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        return pan.substring(Math.max(0, pan.length() - 4));
    }
}
