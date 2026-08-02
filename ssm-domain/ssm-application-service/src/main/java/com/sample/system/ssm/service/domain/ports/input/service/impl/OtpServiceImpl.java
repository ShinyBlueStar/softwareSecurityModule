package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.AuditEventType;
import com.sample.system.ssm.service.domain.enums.OtpStatus;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.model.Otp;
import com.sample.system.ssm.service.domain.ports.input.service.AuditService;
import com.sample.system.ssm.service.domain.ports.input.service.OtpService;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.ports.output.repository.OtpRepository;
import com.sample.system.ssm.service.domain.ports.output.repository.external.VaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component("OTP")
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final VaultRepository vaultRepository;
    private final OtpRepository otpRepository;
    private final AuditService auditService;

    @Value("${ssm.otp.max-retry-attempts:3}")
    private int maxRetryAttempts;

    @Override
    public Card generate(Card card) throws SsmDomainException {
        log.info("OtpServiceImpl.generate started, cardId={}, type: {}", card != null ? card.getCardId() : null, card.getReqType());
        try {
            if (card.getCardId() == null) {
                throw new SsmDomainException(
                        "CardId is required for OTP generation",
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST
                );
            }

            // اگر OTP فعالی که هنوز منقضی نشده وجود دارد، اجازه تولید جدید نده
            var lastOtpOpt = otpRepository.findLastByCardId(card.getCardId());
            if (lastOtpOpt.isPresent()) {
                Otp lastOtp = lastOtpOpt.get();
                Instant now = Instant.now();

                if (lastOtp.status() == OtpStatus.VALID && !lastOtp.isExpiredAt(now)) {
                    log.info("Active OTP already exists for cardId={}", card.getCardId());
                    throw new SsmDomainException(
                            "Active OTP already exists for this card",
                            StatusService.ACTIVE_OTP_EXIST,
                            HttpStatus.BAD_REQUEST
                    );
                }

                // اگر VALID است ولی بر اساس زمان منقضی شده، همان ردیف فعلی را در DB اکسپایر کن
                if (lastOtp.status() == OtpStatus.VALID && lastOtp.isExpiredAt(now)) {
                    otpRepository.markExpired(lastOtp.otpId());
                    log.info("Marked previous OTP as EXPIRED for cardId={}", card.getCardId());
                }
            }

            VaultRepository.OtpGenerationResult otpResult = vaultRepository.generateOtp(card);
            log.info("OTP generated successfully for card: {}", card.getCardId());

            // set encrypted OTP on card to propagate to caller
            card.setEncryptedOtp(otpResult.encryptedOtp());
            card.setValue(otpResult.encryptedOtp());

            Otp otp = new Otp(
                null,
                card.getCardId(),
                otpResult.hashOtp(),
                otpResult.expireAt(),
                0,
                OtpStatus.VALID
            );

            otpRepository.save(otp);
            log.info("OTP persisted successfully for cardId={}", card.getCardId());

            // ثبت رویداد در Audit_Log
            auditService.logEvent(
                    AuditEventType.OTP_GENERATED,
                    "OTP",
                    otp.otpId(),
                    "SYSTEM",
                    Map.of("cardId", card.getCardId().toString())
            );

            return card;
        } catch (SsmDomainException e) {
            log.error("Failed to generate OTP for card: {}", card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating OTP", e);
            throw new SsmDomainException(
                    "OTP generation failed: " + e.getMessage(),
                    StatusService.OTP_GENERATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public Boolean verify(Card card) throws SsmDomainException {
        log.info("OtpServiceImpl.verify started, for cardId={}", card != null ? card.getCardId() : null);

        try {
            if (card.getCardId() == null) {
                throw new SsmDomainException(
                        "CardId is required for OTP verification",
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST
                );
            }

            var otpOpt = otpRepository.findValidByCardId(card.getCardId());
            if (otpOpt.isEmpty()) {
                log.error("otp is null");
                throw new SsmDomainException(
                        "No active OTP found for this card",
                        StatusService.OTP_VERIFICATION_FAILED,
                        HttpStatus.BAD_REQUEST
                );
            }

            Otp otp = otpOpt.get();
            Instant now = Instant.now();

            // وضعیت باید VALID باشد
            if (otp.status() != OtpStatus.VALID) {
                log.error("OTP status is not VALID for cardId={}, status={}", card.getCardId(), otp.status());
                throw new SsmDomainException(
                        "OTP status is not valid",
                        StatusService.OTP_VERIFICATION_FAILED,
                        HttpStatus.BAD_REQUEST
                );
            }

            if (otp.attemptCount() >= maxRetryAttempts) {
                log.error("Maximum OTP retry attempts exceeded for cardId={}", card.getCardId());
                throw new SsmDomainException(
                        "Maximum OTP retry attempts exceeded",
                        StatusService.OTP_VERIFICATION_FAILED,
                        HttpStatus.TOO_MANY_REQUESTS
                );
            }

            if (otp.isExpiredAt(now)) {
                log.error("OTP is expired for cardId={}", card.getCardId());
                throw new SsmDomainException(
                        "OTP has expired",
                        StatusService.OTP_VERIFICATION_FAILED,
                        HttpStatus.BAD_REQUEST
                );
            }

            boolean isValid = vaultRepository.verifyOtp(card);
            log.info("OTP verification {} for cardId: {}", isValid ? "succeeded" : "failed", card.getCardId());

            if (isValid) {
                // When validation succeeds (called from vault), mark OTP as USED
                otpRepository.save(otp.markUsed());
                log.info("OTP marked as USED for cardId={}, otpId={}", card.getCardId(), otp.otpId());
            } else {
                // ناموفق: افزایش شمارنده تلاش‌ها؛ اگر تلاش‌ها ≥ max → OTP به وضعیت LOCK تغییر می‌کند
                Otp updated = otp.incrementAttempt();
                Otp toSave = updated.attemptCount() >= maxRetryAttempts ? updated.lock() : updated;
                otpRepository.save(toSave);
                if (updated.attemptCount() >= maxRetryAttempts) {
                    log.warn("OTP locked after max retry attempts for cardId={}, otpId={}", card.getCardId(), updated.otpId());
                }
            }

            return isValid;
        } catch (SsmDomainException e) {
            log.error("Failed to verify OTP for card: {}", card.getCardId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error verifying OTP", e);
            throw new SsmDomainException(
                    "OTP verification failed: " + e.getMessage(),
                    StatusService.OTP_VERIFICATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
