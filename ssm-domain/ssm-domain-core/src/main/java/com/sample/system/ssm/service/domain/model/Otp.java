package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.OtpStatus;

import java.time.Instant;
import java.util.UUID;

public record Otp(
        UUID otpId,
        UUID cardId,
        String otpHash,
        Instant expireAt,
        int attemptCount,
        OtpStatus status
) {
  public boolean isExpiredAt(Instant now) {
    return now.isAfter(expireAt) || status == OtpStatus.EXPIRED;
  }

  public Otp markUsed() {
    return new Otp(otpId, cardId, otpHash, expireAt, attemptCount, OtpStatus.USED);
  }

  public Otp incrementAttempt() {
    return new Otp(otpId, cardId, otpHash, expireAt, attemptCount + 1, status);
  }

  public Otp lock() {
    return new Otp(otpId, cardId, otpHash, expireAt, attemptCount, OtpStatus.LOCKED);
  }
}
