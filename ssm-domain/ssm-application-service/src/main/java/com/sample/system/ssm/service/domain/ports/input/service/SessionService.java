package com.sample.system.ssm.service.domain.ports.input.service;

import com.sample.system.ssm.service.domain.enums.Channel;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.model.Session;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public interface SessionService {
    Session createSession(UUID cardId, Channel channel, String sessionType,
                          String clientFingerprint) throws SsmDomainException;

    void invalidateSession(UUID sessionId);

    Boolean validateSession(String sessionId);
}
