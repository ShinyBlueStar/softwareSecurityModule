package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.entity.command.OtpCommandEntity;
import com.sample.system.ssm.service.dataaccess.repository.OtpCommandJpaRepository;
import com.sample.system.ssm.service.domain.model.Otp;
import com.sample.system.ssm.service.domain.enums.OtpStatus;
import com.sample.system.ssm.service.domain.ports.output.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {

  private final OtpCommandJpaRepository jpa;

  @Override
  public Otp save(Otp otp) {
    log.info("OtpRepositoryImpl.save started, otpId={}, cardId={}", otp != null ? otp.otpId() : null, otp != null ? otp.cardId() : null);
    var e = new OtpCommandEntity();
    e.setOtpId(otp.otpId());
    e.setCardId(otp.cardId());
    e.setOtpHash(otp.otpHash());
    e.setExpireAt(otp.expireAt());
    e.setAttemptCount(otp.attemptCount());
    e.setStatus(otp.status());
    var saved = jpa.save(e);
    return new Otp(saved.getOtpId(), saved.getCardId(), saved.getOtpHash(),
            saved.getExpireAt(), saved.getAttemptCount(), saved.getStatus());
  }

  @Override
  public Optional<Otp> findValidByCardId(UUID cardId) {
    log.info("OtpRepositoryImpl.findValidByCardId started, cardId={}", cardId);
    Instant now = Instant.now();
    return jpa.findFirstByCardIdAndStatusAndExpireAtAfter(cardId, OtpStatus.VALID, now)
            .map(e -> new Otp(e.getOtpId(), e.getCardId(), e.getOtpHash(),
                    e.getExpireAt(), e.getAttemptCount(), e.getStatus()));
  }

  @Override
  public Optional<Otp> findLastByCardId(UUID cardId) {
    log.info("OtpRepositoryImpl.findLastByCardId started, cardId={}", cardId);
    return jpa.findFirstByCardIdOrderByExpireAtDesc(cardId)
            .map(e -> new Otp(e.getOtpId(), e.getCardId(), e.getOtpHash(),
                    e.getExpireAt(), e.getAttemptCount(), e.getStatus()));
  }

  @Override
  public void markExpired(UUID otpId) {
    log.info("OtpRepositoryImpl.markExpired started, otpId={}", otpId);
    jpa.findById(otpId).ifPresent(e -> {
      e.setStatus(OtpStatus.EXPIRED);
      jpa.save(e);
    });
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public int markExpiredOtps(Instant now) {
    log.info("OtpRepositoryImpl.markExpiredOtps started, now={}", now);
    int count = jpa.markExpiredOtps(now, OtpStatus.VALID, OtpStatus.EXPIRED);
    if (count > 0) {
      log.info("Marked {} OTP(s) as EXPIRED", count);
    }
    return count;
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public int deleteExpiredOtps() {
    log.info("OtpRepositoryImpl.deleteExpiredOtps started");
    java.util.List<OtpStatus> statuses = java.util.List.of(OtpStatus.EXPIRED, OtpStatus.USED);
    int count = jpa.deleteByStatusIn(statuses);
    if (count > 0) {
      log.info("Deleted {} OTP(s) with status EXPIRED or USED", count);
    }
    return count;
  }
}