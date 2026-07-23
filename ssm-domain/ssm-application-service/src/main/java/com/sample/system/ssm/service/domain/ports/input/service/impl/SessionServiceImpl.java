package com.sample.system.ssm.service.domain.ports.input.service.impl;

import com.sample.system.ssm.service.domain.enums.Channel;
import com.sample.system.ssm.service.domain.enums.SessionStatus;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Session;
import com.sample.system.ssm.service.domain.ports.input.service.SessionService;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepo;
    private final Clock clock;

    public SessionServiceImpl(SessionRepository sessionRepo, Clock clock) {
        this.sessionRepo = sessionRepo;
        this.clock = clock;
    }


    public Session createSession(UUID cardId, Channel channel, String sessionType,
                                 String clientFingerprint) throws SsmDomainException {
        if (sessionRepo.findActiveByCardId(cardId).isPresent()) {
            throw new SsmDomainException(
                    "Active session already exists for this card",
                    StatusService.ACTIVE_SESSION_EXISTS,
                    HttpStatus.BAD_REQUEST
            );
        }

        Instant now = clock.instant();
        UUID sessionId = UUID.randomUUID();
        // Default session validity: 30 minutes
        Instant expiresAt = now.plusSeconds(30 * 60);

        Session session = new Session(
                sessionId,
                cardId,
                channel != null ? channel.name() : null,
                sessionType != null ? sessionType : "DEFAULT",
                SessionStatus.ACTIVE,
                clientFingerprint,
                now,
                expiresAt
        );
        
        log.info("Creating session: sessionId={}, cardId={}, channel={}, sessionType={}",
            sessionId, cardId, channel, sessionType);
        
        return sessionRepo.save(session);
    }

    public void invalidateSession(UUID sessionId) {
        if (sessionRepo.markInvalidated(sessionId)) {
            log.info("Session invalidated: sessionId={}", sessionId);
        } else {
            log.warn("Session not found for invalidation: sessionId={}", sessionId);
        }
    }

    @Override
    public Boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("Session validation failed: sessionId is null or blank");
            return false;
        }

        return sessionRepo.findBySessionId(sessionId)
                .map(session -> {
                    Instant now = clock.instant();
                    boolean isValid = session.isValid() && now.isBefore(session.getExpiresAt());
                    
                    if (!isValid) {
                        log.warn("Session validation failed: sessionId={}, expired={}, active={}", 
                                sessionId, now.isAfter(session.getExpiresAt()), session.isActive());
                    } else {
                        log.debug("Session validated successfully: sessionId={}", sessionId);
                    }
                    
                    return isValid;
                })
                .orElseGet(() -> {
                    log.warn("Session not found: sessionId={}", sessionId);
                    return false;
                });
    }
}
