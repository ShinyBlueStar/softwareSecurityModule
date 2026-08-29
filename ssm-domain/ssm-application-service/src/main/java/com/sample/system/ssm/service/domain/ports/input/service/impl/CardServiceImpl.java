package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.command.TransactionContext;
import com.sample.system.ssm.service.domain.enums.*;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.ports.input.service.*;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import com.sample.system.ssm.service.domain.response.UnblockCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Card Service: generate (PIN/OTP/CVV; persist PIN1 only; CVV2 never stored – PCI), verify, viewSecret, validateSecret.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final String CVV2_VIEW_FORBIDDEN = "CVV2 cannot be viewed; it is not stored (PCI compliance)";
    private static final String INVALID_REQUEST_TYPE_MSG = "Invalid request type: %s. Supported types: PIN, OTP, CVV";

    private final Map<String, SecurityServiceProcessor> processorMap;
    private final SessionService sessionService;
    private final CardSecretRepository cardSecretRepository;
    private final SessionRepository sessionRepository;
    private final LockService lockService;
    private final AuditService auditService;
    private final RetryPolicyService retryPolicyService;
    private final RateLimitService rateLimitService;
    private final CvvService cvvService;
    private final PinService pinService;

    @Override
    public Card generate(Card card) throws SsmDomainException {
        Objects.requireNonNull(card, "Card data must not be null");
        requireNonBlank(card.getSessionId().toString(), "Session ID must not be empty");
        requireNonBlank(card.getReqType().name(), "Request type must not be empty");
        validateSession(card.getSessionId());
        UUID actualCardId = getCardIdFromSession(card.getSessionId());
        checkLockState(actualCardId);
        card.setCardId(actualCardId);
        var upperReqType = card.getReqType().name();
        // اعمال محدودیت نرخ (Rate Limiting) برای تولید OTP/PIN/CVV
        rateLimitService.checkGenerateRateLimit(actualCardId, upperReqType);
        var processor = getProcessor(upperReqType);
        Card answer = processor.generate(card);
        log.info("value with type:{} generated for SessionId: {}",card.getReqType(), card.getSessionId());
        return answer;
    }

    @Override
    public Card verify(Card card) throws SsmDomainException {
        requireNonBlank(card.getSessionId().toString(), "Session ID must not be empty");
        requireNonBlank(card.getReqType().name(), "Request type must not be empty");
        var sessionId = card.getSessionId();
        validateSession(sessionId);
        var upperReqType = card.getReqType().name();
        UUID actualCardId = getCardIdFromSession(sessionId);
        var processor = getProcessor(upperReqType);
        checkLockState(actualCardId);
        if (retryPolicyService.isRetryLimitExceeded(actualCardId, card.getReqType())) {
            lockService.applyPermanentLock(LockScope.CARD, actualCardId.toString(), LockReason.RETRY_LIMIT_EXCEEDED);
            auditService.logEvent(AuditEventType.RETRY_EXCEEDED, upperReqType, actualCardId, "SYSTEM", null);
            throw new SsmDomainException(
                    "Retry limit exceeded",
                    StatusService.RATE_LIMIT_EXCEEDED,
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }
        card.setCardId(actualCardId);
        log.info("Verifying {} for session: {}", card.getReqType(), sessionId);
        boolean success = Boolean.TRUE.equals(processor.verify(card));
        applyRetryPolicyAfterVerify(actualCardId, card.getReqType(), success);

        // ثبت رویداد در Audit_Log با جزئیات session، card_id و در صورت وجود IP
        Map<String, Object> auditMetadata = new HashMap<>(Map.of(
                "result", success ? "SUCCESS" : "FAIL",
                "attemptType", upperReqType,
                "sessionId", sessionId.toString(),
                "cardId", actualCardId.toString()
        ));
        auditService.logEvent(
                AuditEventType.VALIDATION_ATTEMPT,
                "CARD",
                actualCardId,
                "SYSTEM",
                auditMetadata
        );

        card.setValue(success ? "valid" : "invalid");
        log.info("Verification {} for {} in session: {}", success ? "succeeded" : "failed", card.getReqType(), sessionId);
        return card;
    }

    @Override
    public String viewSecret(UUID sessionId, UUID cardId, SecretType secretType) throws SsmDomainException {
        if (secretType == SecretType.CVV) {
            throw new SsmDomainException(
                    CVV2_VIEW_FORBIDDEN,
                    StatusService.USER_HAS_NOT_PERMISSION,
                    HttpStatus.FORBIDDEN
            );
        }
        validateSession(sessionId);
        UUID actualCardId = cardId != null ? cardId : getCardIdFromSession(sessionId);
        checkLockState(actualCardId);
        rateLimitService.checkViewSecretRateLimit(actualCardId, secretType.name());

        var cardSecret = cardSecretRepository.findActiveByCardIdAndType(actualCardId, secretType)
                .orElseThrow(() -> new SsmDomainException(
                        "Card secret not found: cardId=" + actualCardId + ", type=" + secretType,
                        StatusService.ID_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        auditService.logEvent(
                secretType == SecretType.PIN ? AuditEventType.PIN1_VIEWED : AuditEventType.CVV2_VIEWED,
                "CARD_SECRET",
                cardSecret.cardSecretId(),
                "SYSTEM",
                Map.of("cardId", actualCardId.toString(), "sessionId", sessionId.toString())
        );
        log.info("Card secret viewed: secretType={}, cardId={}", secretType, actualCardId);
        return cardSecret.encryptedValue();
    }

    // ——— Helpers ———
    private static void requireNonBlank(String value, String message) throws SsmDomainException {
        if (value == null || value.isBlank()) {
            throw new SsmDomainException(
                    message,
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private SecurityServiceProcessor getProcessor(String reqType) throws SsmDomainException {
        return Optional.ofNullable(processorMap.get(reqType.toUpperCase()))
                .orElseThrow(() -> new SsmDomainException(
                        INVALID_REQUEST_TYPE_MSG.formatted(reqType),
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST
                ));
    }

    private void applyRetryPolicyAfterVerify(UUID cardId, SecretType attemptType, boolean success) throws SsmDomainException {
        if (success) {
            retryPolicyService.resetRetryCount(cardId, attemptType);
        } else {
            retryPolicyService.recordAttempt(cardId, attemptType, false, "system");
            if (retryPolicyService.isRetryLimitExceeded(cardId, attemptType)) {
                lockService.applyPermanentLock(LockScope.CARD, cardId.toString(), LockReason.RETRY_LIMIT_EXCEEDED);
                auditService.logEvent(AuditEventType.RETRY_EXCEEDED, "CARD", cardId, "SYSTEM", null);
                log.warn("Verify retry limit exceeded: card locked, cardId={}, attemptType={}", cardId, attemptType);
            }
        }
    }

    private static Map<String, Object> validationMetadata(TransactionContext ctx, boolean isValid) {
        return Map.of(
                "transactionId", ctx.transactionId(),
                "amount", ctx.amount(),
                "currency", ctx.currency(),
                "merchantId", ctx.merchantId() != null ? ctx.merchantId() : "N/A",
                "channel", ctx.channel().name(),
                "result", isValid ? "SUCCESS" : "FAIL"
        );
    }

    private UUID getCardIdFromSession(UUID sessionId) throws SsmDomainException {
        return sessionRepository.findBySessionId(sessionId.toString())
                .map(session -> {
                    if (session.getCardId() != null) {
                        return session.getCardId();
                    }
                    throw new IllegalStateException("CardId is null in session: " + sessionId);
                })
                .orElseThrow(() -> new SsmDomainException(
                        "Session not found: " + sessionId,
                        StatusService.ID_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateSession(UUID sessionId) throws SsmDomainException {
        if (!Boolean.TRUE.equals(sessionService.validateSession(sessionId.toString()))) {
            throw new SsmDomainException(
                    "Session is invalid or expired",
                    StatusService.SESSION_NOT_VALID,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private void checkLockState(UUID cardId) throws SsmDomainException {
        if (lockService.isLocked(LockScope.CARD, cardId)) {
            throw new SsmDomainException(
                    "Card is locked",
                    StatusService.CARD_IS_LOCKED,
                    HttpStatus.LOCKED
            );
        }
    }

    @Override
    public UnblockCardResponse unblockCard(UUID sessionId) throws SsmDomainException {
        validateSession(sessionId);
        UUID cardId = getCardIdFromSession(sessionId);
        var activeLock = lockService.findActiveLock(LockScope.CARD, cardId);
        if (activeLock.isPresent()) {
            var lock = activeLock.get();
            lockService.releaseLock(lock.lockId());
            auditService.logEvent(
                    AuditEventType.LOCK_RELEASED,
                    "CARD",
                    cardId,
                    "SYSTEM",
                    Map.of("sessionId", sessionId.toString(), "lockId", lock.lockId().toString())
            );
            log.info("Card unblocked: cardId={}, sessionId={}", cardId, sessionId);
            return new UnblockCardResponse(cardId, true, "Card unblocked");
        }
        log.info("Card was not locked: cardId={}, sessionId={}", cardId, sessionId);
        return new UnblockCardResponse(cardId, false, "Card was not locked");
    }
}
