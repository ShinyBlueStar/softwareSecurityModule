package com.sample.system.ssm.service.domain.response;

import java.time.Instant;
import java.util.UUID;

public record GenerateOtpResponse(
    UUID sessionId,
    String otp,
    Instant validUntil
) {}
