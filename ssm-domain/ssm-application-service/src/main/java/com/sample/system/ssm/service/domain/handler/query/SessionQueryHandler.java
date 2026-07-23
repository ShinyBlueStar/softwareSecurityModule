package com.sample.system.ssm.service.domain.handler.query;

import com.sample.system.ssm.service.domain.ports.input.service.SessionService;
import com.sample.system.ssm.service.domain.response.InvalidateSessionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class SessionQueryHandler {
    SessionService sessionService;

    public void invalidate(@PathVariable UUID sessionId) {
        log.debug("SessionQueryHandler.invalidate started, sessionId={}", sessionId);
        sessionService.invalidateSession(sessionId);
    }
}
