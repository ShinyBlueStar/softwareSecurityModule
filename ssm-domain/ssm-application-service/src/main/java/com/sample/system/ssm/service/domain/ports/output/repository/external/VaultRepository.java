package com.sample.system.ssm.service.domain.ports.output.repository.external;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;

/**
 * Repository interface for communicating with vault-service
 * Following Hexagonal Architecture - Output Port
 */
public interface VaultRepository {
    
    /**
     * Generate PIN1 for the given PAN
     * @return Generated PIN (encrypted and hash) - NO plain value for security
     * @throws SsmDomainException if generation fails
     */
    PinGenerationResult generatePin(Card card) throws SsmDomainException;
    
    /**
     * Verify PIN1
     * @return true if PIN is valid, false otherwise
     * @throws SsmDomainException if verification fails
     */
    boolean verifyPin(Card card) throws SsmDomainException;
    
    /**
     * Generate OTP (PIN2) for the given PAN
     * @return Generated OTP data including encrypted value, hash and expiry time
     * @throws SsmDomainException if generation fails
     */
    OtpGenerationResult generateOtp(Card card) throws SsmDomainException;
    
    /**
     * Verify OTP
     * @return true if OTP is valid, false otherwise
     * @throws SsmDomainException if verification fails
     */
    boolean verifyOtp(Card card) throws SsmDomainException;
    
    /**
     * Generate CVV2 for the given card data
     * @return Generated CVV2
     * @throws SsmDomainException if generation fails
     */
    CvvGenerationResult generateCvv2(Card card) throws SsmDomainException;
    
    /**
     * Verify CVV2
     * @return true if CVV2 is valid, false otherwise
     * @throws SsmDomainException if verification fails
     */
    boolean verifyCvv2(Card card) throws SsmDomainException;
    
    /**
     * Result of PIN generation containing encrypted PIN and hash
     * Note: No plain PIN is returned for security reasons.
     * Decryption happens only in Card Module.
     */
    record PinGenerationResult(String encryptedPin, String hashPin) {}
    record CvvGenerationResult(String encryptedCvv, String hashCvv) {}
    record OtpGenerationResult(String encryptedOtp, String hashOtp, java.time.Instant expireAt) {}
}
