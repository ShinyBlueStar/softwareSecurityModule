package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.model.Otp;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
  Otp save(Otp otp);
  Optional<Otp> findValidByCardId(UUID cardId);
  Optional<Otp> findLastByCardId(UUID cardId);
  void markExpired(UUID otpId);
  int markExpiredOtps(Instant now);
  int deleteExpiredOtps();
}
