package com.sample.system.ssm.service.domain.response;

import java.util.UUID;

public record InvalidateSessionResponse(UUID sessionId, Boolean invalidate) {
}
