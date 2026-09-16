package com.sample.system.ssm.service.dataaccess.entity.command;

import com.sample.system.ssm.service.domain.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SSM_OTPS", indexes = {
        @Index(name = "IDX_SSM_OTPS_CARD", columnList = "CARD_ID")
})
public class OtpCommandEntity {

  @Id
  @Column(name = "OTP_ID", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID otpId;

  @Column(name = "CARD_ID", nullable = false)
  private UUID cardId;

  @Column(name = "OTP_HASH", nullable = false, length = 512)
  private String otpHash;

  @Column(name = "EXPIRE_AT", nullable = false)
  private Instant expireAt;

  @Column(name = "ATTEMPT_COUNT", nullable = false)
  private int attemptCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, length = 20)
  private OtpStatus status;

}
