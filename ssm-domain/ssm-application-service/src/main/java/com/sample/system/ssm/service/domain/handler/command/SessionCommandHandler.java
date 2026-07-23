package com.sample.system.ssm.service.domain.handler.command;

import com.sample.system.ssm.service.domain.command.CreateSessionCommand;
import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.SessionService;
import com.sample.system.ssm.service.domain.response.CreateSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCommandHandler {
    private final SessionService sessionService;

    public CreateSessionResponse createSession(CreateSessionCommand req) throws SsmDomainException {
        var session = sessionService.createSession(
            req.cardId(),
            req.channel(),
            req.sessionType(),
            req.clientFingerprint()
        );
        
        return new CreateSessionResponse(
            session.getSessionId(),
            session.getExpiresAt()
        );
    }
}
