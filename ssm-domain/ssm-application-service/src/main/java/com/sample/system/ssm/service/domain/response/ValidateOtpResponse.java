package com.sample.system.ssm.service.domain.response;

public record ValidateOtpResponse(
    boolean ok,
    String reason
) {}
