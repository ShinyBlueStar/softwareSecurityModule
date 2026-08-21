package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.CardSecretStatus;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.model.CardSecret;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.output.repository.CardSecretRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CvvServiceImplTest {

    @Mock
    private VaultRepository vaultRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private CardSecretRepository cardSecretRepository;

    @InjectMocks
    private CvvServiceImpl cvvService;

    @Test
    void generate_rejectsRequest_withoutEncryptedData() throws SsmDomainException {
        Card card = new Card();
        card.setCardId(UUID.randomUUID());
        card.setEncryptedData(null);

        assertThatThrownBy(() -> cvvService.generate(card))
                .isInstanceOf(SsmDomainException.class);

        verify(vaultRepository, never()).generateCvv2(any());
    }

    @Test
    void generate_rejectsRequest_withBlankEncryptedData() throws SsmDomainException {
        Card card = new Card();
        card.setCardId(UUID.randomUUID());
        card.setEncryptedData("   ");

        assertThatThrownBy(() -> cvvService.generate(card))
                .isInstanceOf(SsmDomainException.class);

        verify(vaultRepository, never()).generateCvv2(any());
    }

    @Test
    void generate_savesNewCardSecret_whenNoneExistsYet() throws SsmDomainException {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setCardId(cardId);
        card.setEncryptedData("base64-blob");

        when(vaultRepository.generateCvv2(card))
                .thenReturn(new VaultRepository.CvvGenerationResult("enc-cvv", "hash-cvv"));
        when(cardSecretRepository.findActiveByCardIdAndType(cardId, SecretType.CVV))
                .thenReturn(Optional.empty());

        Card result = cvvService.generate(card);

        assertThat(result.getValue()).isEqualTo("enc-cvv");
        assertThat(result.getEncryptedCvv()).isEqualTo("enc-cvv");
        verify(cardSecretRepository).save(any(CardSecret.class));
        verify(auditService).logEvent(any(), any(), any(), any(), any());
    }

    @Test
    void verify_throwsWhenNoCvvSecretWasEverGenerated() {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setCardId(cardId);

        when(cardSecretRepository.findActiveByCardIdAndType(cardId, SecretType.CVV))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cvvService.verify(card))
                .isInstanceOf(SsmDomainException.class);
    }

    @Test
    void verify_delegatesToVault_whenSecretExists() throws SsmDomainException {
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setCardId(cardId);
        card.setValue("client-encrypted-value");

        CardSecret stored = new CardSecret(
                UUID.randomUUID(), cardId, SecretType.CVV, "enc-cvv",
                Optional.of("hash-cvv"), Optional.empty(), CardSecretStatus.ACTIVE,
                Instant.now(), Optional.empty()
        );
        when(cardSecretRepository.findActiveByCardIdAndType(cardId, SecretType.CVV))
                .thenReturn(Optional.of(stored));
        when(vaultRepository.verifyCvv2(card)).thenReturn(true);

        Boolean result = cvvService.verify(card);

        assertThat(result).isTrue();
        assertThat(card.getHashedCvv()).isEqualTo("hash-cvv");
    }
}
