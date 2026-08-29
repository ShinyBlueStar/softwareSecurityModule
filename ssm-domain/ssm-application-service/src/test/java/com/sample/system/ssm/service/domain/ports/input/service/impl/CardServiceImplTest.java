package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.LockReason;
import com.sample.system.ssm.service.domain.enums.LockScope;
import com.sample.system.ssm.service.domain.enums.LockStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.model.LockState;
import com.sample.system.ssm.service.domain.model.Session;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.input.service.CvvService;
import com.sample.system.ssm.service.domain.ports.input.service.LockService;
import com.sample.system.ssm.service.domain.ports.input.service.PinService;
import com.sample.system.ssm.service.domain.ports.input.service.RateLimitService;
import com.sample.system.ssm.service.domain.ports.input.service.RetryPolicyService;
import com.sample.system.ssm.service.domain.ports.input.service.SecurityServiceProcessor;
import com.sample.system.ssm.service.domain.ports.input.service.SessionService;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import com.sample.system.ssm.service.domain.response.UnblockCardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private SessionService sessionService;
    @Mock
    private CardSecretRepository cardSecretRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private LockService lockService;
    @Mock
    private AuditService auditService;
    @Mock
    private RetryPolicyService retryPolicyService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private CvvService cvvService;
    @Mock
    private PinService pinService;
    @Mock
    private SecurityServiceProcessor pinProcessor;

    private CardServiceImpl cardService;

    private final UUID sessionId = UUID.randomUUID();
    private final UUID cardId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(
                Map.of("PIN", pinProcessor),
                sessionService, cardSecretRepository, sessionRepository,
                lockService, auditService, retryPolicyService, rateLimitService,
                cvvService, pinService
        );
    }

    private Card cardRequest(SecretType type) {
        Card card = new Card();
        card.setSessionId(sessionId);
        card.setReqType(type);
        return card;
    }

    private void stubValidActiveSession() {
        when(sessionService.validateSession(sessionId.toString())).thenReturn(true);
        Session session = new Session(sessionId, cardId, "WEB", "PIN_CHANGE", null, null, Instant.now(), Instant.now().plusSeconds(300));
        when(sessionRepository.findBySessionId(sessionId.toString())).thenReturn(Optional.of(session));
    }

    @Test
    void generate_delegatesToTheMatchingProcessor_whenSessionAndCardAreUnlocked() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.isLocked(LockScope.CARD, cardId)).thenReturn(false);
        Card generated = cardRequest(SecretType.PIN);
        generated.setValue("encrypted-pin");
        when(pinProcessor.generate(any(Card.class))).thenReturn(generated);

        Card result = cardService.generate(cardRequest(SecretType.PIN));

        assertThat(result.getValue()).isEqualTo("encrypted-pin");
        verify(rateLimitService).checkGenerateRateLimit(cardId, "PIN");
    }

    @Test
    void generate_throwsWhenSessionIsInvalid() {
        when(sessionService.validateSession(sessionId.toString())).thenReturn(false);

        assertThatThrownBy(() -> cardService.generate(cardRequest(SecretType.PIN)))
                .isInstanceOf(SsmDomainException.class);
    }

    @Test
    void generate_throwsWhenCardIsLocked() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.isLocked(LockScope.CARD, cardId)).thenReturn(true);

        assertThatThrownBy(() -> cardService.generate(cardRequest(SecretType.PIN)))
                .isInstanceOf(SsmDomainException.class);

        verify(pinProcessor, never()).generate(any());
    }

    @Test
    void verify_appliesPermanentLock_whenRetryLimitAlreadyExceeded() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.isLocked(LockScope.CARD, cardId)).thenReturn(false);
        when(retryPolicyService.isRetryLimitExceeded(cardId, SecretType.PIN)).thenReturn(true);

        assertThatThrownBy(() -> cardService.verify(cardRequest(SecretType.PIN)))
                .isInstanceOf(SsmDomainException.class);

        verify(lockService).applyPermanentLock(LockScope.CARD, cardId.toString(), LockReason.RETRY_LIMIT_EXCEEDED);
        verify(pinProcessor, never()).verify(any());
    }

    @Test
    void verify_resetsRetryCount_onSuccessfulVerification() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.isLocked(LockScope.CARD, cardId)).thenReturn(false);
        when(retryPolicyService.isRetryLimitExceeded(cardId, SecretType.PIN)).thenReturn(false);
        when(pinProcessor.verify(any(Card.class))).thenReturn(true);

        Card result = cardService.verify(cardRequest(SecretType.PIN));

        assertThat(result.getValue()).isEqualTo("valid");
        verify(retryPolicyService).resetRetryCount(cardId, SecretType.PIN);
        verify(retryPolicyService, never()).recordAttempt(any(), any(), eq(false), anyString());
    }

    @Test
    void verify_recordsFailedAttempt_onFailedVerification() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.isLocked(LockScope.CARD, cardId)).thenReturn(false);
        when(retryPolicyService.isRetryLimitExceeded(cardId, SecretType.PIN)).thenReturn(false);
        when(pinProcessor.verify(any(Card.class))).thenReturn(false);

        Card result = cardService.verify(cardRequest(SecretType.PIN));

        assertThat(result.getValue()).isEqualTo("invalid");
        verify(retryPolicyService).recordAttempt(cardId, SecretType.PIN, false, "system");
    }

    @Test
    void viewSecret_rejectsCvv_becauseItIsNeverStored() {
        assertThatThrownBy(() -> cardService.viewSecret(sessionId, cardId, SecretType.CVV))
                .isInstanceOf(SsmDomainException.class)
                .hasMessageContaining("CVV2 cannot be viewed");
    }

    @Test
    void unblockCard_releasesLock_whenOneIsActive() throws SsmDomainException {
        stubValidActiveSession();
        UUID lockId = UUID.randomUUID();
        LockState activeLock = new LockState(lockId, LockScope.CARD, cardId.toString(),
                LockReason.RETRY_LIMIT_EXCEEDED, Optional.empty(), LockStatus.ACTIVE, Instant.now(), Optional.empty());
        when(lockService.findActiveLock(LockScope.CARD, cardId)).thenReturn(Optional.of(activeLock));

        UnblockCardResponse response = cardService.unblockCard(sessionId);

        assertThat(response.unblocked()).isTrue();
        verify(lockService).releaseLock(lockId);
    }

    @Test
    void unblockCard_reportsNotLocked_whenNoActiveLockExists() throws SsmDomainException {
        stubValidActiveSession();
        when(lockService.findActiveLock(LockScope.CARD, cardId)).thenReturn(Optional.empty());

        UnblockCardResponse response = cardService.unblockCard(sessionId);

        assertThat(response.unblocked()).isFalse();
        verify(lockService, never()).releaseLock(any());
    }
}
