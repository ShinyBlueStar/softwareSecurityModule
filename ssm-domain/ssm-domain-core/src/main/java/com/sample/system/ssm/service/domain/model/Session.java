package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.Channel;
import com.sample.system.ssm.service.domain.enums.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public class Session {
    private UUID sessionId;
    private UUID cardId;
    private String channel;
    private String sessionType;
    private SessionStatus status;
    private String clientFingerprint;
    private Instant createdAt;
    private Instant expiresAt;

    public Session(UUID sessionId, UUID cardId, String channel, String sessionType, SessionStatus status,
                   String clientFingerprint, Instant createdAt, Instant expiresAt) {
        this.sessionId = sessionId;
        this.cardId = cardId;
        this.channel = channel;
        this.sessionType = sessionType;
        this.status = status != null ? status : SessionStatus.ACTIVE;
        this.clientFingerprint = clientFingerprint;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Session() {
    }

    public static Session create(UUID cardId, String channel, String sessionType, String clientFingerprint, int validitySeconds) {
        Instant now = Instant.now();
        UUID sessionId = UUID.randomUUID();
        Instant expiresAt = now.plusSeconds(validitySeconds);
        return new Session(sessionId, cardId, channel, sessionType != null ? sessionType : "DEFAULT",
                SessionStatus.ACTIVE, clientFingerprint, now, expiresAt);
    }

    public boolean isValid() {
        return status == SessionStatus.ACTIVE && Instant.now().isBefore(expiresAt);
    }

    public void expire() {
        this.status = SessionStatus.EXPIRED;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getCardId() {
        return cardId;
    }

    public String getChannel() {
        return channel;
    }

    public String getSessionType() {
        return sessionType;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public String getClientFingerprint() {
        return clientFingerprint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** @deprecated Use {@link #getStatus()} == SessionStatus.ACTIVE */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }
}

