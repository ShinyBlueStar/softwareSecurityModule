package com.sample.system.ssm.service.domain.response;

import java.time.Instant;
import java.util.UUID;

public record CreateSessionResponse(
    UUID sessionId,
    Instant expireAt
) {}
