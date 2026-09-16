package com.sample.system.ssm.service.dataaccess.entity.command;

import com.sample.system.ssm.service.domain.enums.Channel;
import com.sample.system.ssm.service.domain.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SSM_SESSIONS")
public class SessionCommandEntity {

  @Id
  @Column(name = "SESSION_ID", nullable = false)
  private UUID sessionId;

  @Column(name = "CARD_ID", nullable = false)
  private UUID cardId;

  @Enumerated(EnumType.STRING)
  @Column(name = "CHANNEL", nullable = false, length = 20)
  private Channel channel;

  @Column(name = "SESSION_TYPE", nullable = false, length = 40)
  private String sessionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, length = 20)
  private SessionStatus status;

  @Column(name = "CLIENT_FINGERPRINT", length = 255)
  private String clientFingerprint;

  @Column(name = "CREATED_AT", nullable = false)
  private Instant createdAt;

  @Column(name = "EXPIRE_AT", nullable = false)
  private Instant expireAt;

}
