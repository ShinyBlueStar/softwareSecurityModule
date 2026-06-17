package com.sample.system.ssm.service.domain.ports.output.repository;

import com.sample.system.ssm.service.domain.model.Session;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
  Session save(Session session);
  Optional<Session> findById(UUID sessionId);
  Optional<Session> findBySessionId(String sessionId);

  /**
   * Finds an active (non-expired) session for the given card ID, if any.
   */
  Optional<Session> findActiveByCardId(UUID cardId);

  /**
   * Sets the session status to INVALIDATED and saves. Returns true if the session was found and updated.
   */
  boolean markInvalidated(UUID sessionId);

  /**
   * Finds sessions that have expired by time (expireAt &lt; now) but are still ACTIVE,
   * locks them for update (blocking concurrent reads), and sets their status to EXPIRED.
   *
   * @param now current time
   * @return number of sessions updated to EXPIRED
   */
  int markExpiredSessions(Instant now);

  int deleteExpiredSessions();
}
