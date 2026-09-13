package com.sample.system.ssm.service.dataaccess.adapter.external;

import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mock implementation of VaultRepository for local/testing when vault-service is not available.
 * Returns deterministic stub data. Use profile "test" or "local" to activate.
 */
@Slf4j
@Component
@Profile({"test", "local", "mock-vault"})
public class MockVaultRepository implements VaultRepository {

    private static final String MOCK_ENCRYPTED_PIN = "mock-encrypted-pin-" + System.currentTimeMillis();
    private static final String MOCK_HASH_PIN = "mock-hash-pin";
    private static final String MOCK_ENCRYPTED_OTP = "mock-encrypted-otp";
    private static final String MOCK_HASH_OTP = "mock-hash-otp";
    private static final String MOCK_ENCRYPTED_CVV = "mock-encrypted-cvv";
    private static final String MOCK_HASH_CVV = "mock-hash-cvv";

    @Override
    public PinGenerationResult generatePin(Card card) {
        log.debug("MockVaultRepository.generatePin for cardId={}", card != null ? card.getCardId() : null);
        return new PinGenerationResult(MOCK_ENCRYPTED_PIN, MOCK_HASH_PIN);
    }

    @Override
    public boolean verifyPin(Card card) {
        log.debug("MockVaultRepository.verifyPin for cardId={}", card != null ? card.getCardId() : null);
        // Accept any non-null hashedPin or encryptedPin as valid for testing
        boolean hasPin = card != null && (
                (card.getHashedPin() != null && !card.getHashedPin().isBlank()) ||
                        (card.getEncryptedPin() != null && !card.getEncryptedPin().isBlank())
        );
        return hasPin;
    }

    @Override
    public OtpGenerationResult generateOtp(Card card) {
        log.debug("MockVaultRepository.generateOtp for cardId={}", card != null ? card.getCardId() : null);
        Instant expireAt = Instant.now().plusSeconds(120); // 2 minutes
        return new OtpGenerationResult(MOCK_ENCRYPTED_OTP, MOCK_HASH_OTP, expireAt);
    }

    @Override
    public boolean verifyOtp(Card card) {
        log.debug("MockVaultRepository.verifyOtp for cardId={}", card != null ? card.getCardId() : null);
        // For tests: consider valid if value/encryptedOtp is present
        return card != null && card.getValue() != null && !card.getValue().isBlank();
    }

    @Override
    public CvvGenerationResult generateCvv2(Card card) {
        log.debug("MockVaultRepository.generateCvv2 for cardId={}", card != null ? card.getCardId() : null);
        return new CvvGenerationResult(MOCK_ENCRYPTED_CVV, MOCK_HASH_CVV);
    }

    @Override
    public boolean verifyCvv2(Card card) {
        log.debug("MockVaultRepository.verifyCvv2 for cardId={}", card != null ? card.getCardId() : null);
        return card != null && (
                (card.getEncryptedCvv() != null && !card.getEncryptedCvv().isBlank()) ||
                        (card.getValue() != null && !card.getValue().isBlank())
        );
    }
}
