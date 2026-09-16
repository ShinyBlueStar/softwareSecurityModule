package com.sample.system.ssm.service.dataaccess.adapter;

import com.sample.system.ssm.service.dataaccess.entity.command.SessionCommandEntity;
import com.sample.system.ssm.service.dataaccess.mapper.SessionDataAccessMapper;
import com.sample.system.ssm.service.dataaccess.repository.SessionCommandJpaRepository;
import com.sample.system.ssm.service.domain.enums.SessionStatus;
import com.sample.system.ssm.service.domain.model.Session;
import com.sample.system.ssm.service.domain.ports.output.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionCommandJpaRepository jpa;
    private final SessionDataAccessMapper mapper;

    @Override
    public Session save(Session session) {
        log.debug("SessionRepositoryImpl.save started, sessionId={}", session != null ? session.getSessionId() : null);
        SessionCommandEntity entity = mapper.toEntity(session);
        SessionCommandEntity saved = jpa.save(entity);
        log.info("Session saved to DB: sessionId={}", saved.getSessionId());
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Session> findById(UUID sessionId) {
        log.debug("SessionRepositoryImpl.findById started, sessionId={}", sessionId);
        return jpa.findById(sessionId).map(mapper::toDomain);
    }

    @Override
    public Optional<Session> findBySessionId(String sessionId) {
        log.debug("SessionRepositoryImpl.findBySessionId started, sessionId={}", sessionId);
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        try {
            return jpa.findById(UUID.fromString(sessionId)).map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sessionId format: {}", sessionId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Session> findActiveByCardId(UUID cardId) {
        log.debug("SessionRepositoryImpl.findActiveByCardId started, cardId={}", cardId);
        if (cardId == null) {
            return Optional.empty();
        }
        Instant now = Instant.now();
        return jpa.findFirstByCardIdAndStatusAndExpireAtAfter(cardId, SessionStatus.ACTIVE, now)
                .map(mapper::toDomain);
    }

    @Override
    public boolean markInvalidated(UUID sessionId) {
        log.debug("SessionRepositoryImpl.markInvalidated started, sessionId={}", sessionId);
        return jpa.findById(sessionId)
                .map(entity -> {
                    entity.setStatus(SessionStatus.INVALIDATED);
                    jpa.save(entity);
                    log.info("Session status set to INVALIDATED: sessionId={}", sessionId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public int markExpiredSessions(Instant now) {
        log.debug("SessionRepositoryImpl.markExpiredSessions started, now={}", now);
        int count = jpa.markExpiredSessions(now, SessionStatus.ACTIVE, SessionStatus.EXPIRED);
        if (count > 0) {
            log.info("Marked {} session(s) as EXPIRED", count);
        }
        return count;
    }

    @Override
    @Transactional
    public int deleteExpiredSessions() {
        log.debug("SessionRepositoryImpl.deleteExpiredSessions started");
        List a = new ArrayList();
        a.add(SessionStatus.EXPIRED);
        a.add(SessionStatus.INVALIDATED);
        int count = jpa.deleteByStatusIn(SessionStatus.terminalStatuses());
        if (count > 0) {
            log.info("Deleted {} session(s) with status EXPIRED", count);
        }
        return count;
    }
}
