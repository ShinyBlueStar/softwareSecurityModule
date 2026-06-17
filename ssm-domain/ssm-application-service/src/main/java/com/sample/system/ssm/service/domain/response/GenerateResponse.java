package com.sample.system.ssm.service.domain.response;

/**
 * Response for generate operations (PIN/OTP/CVV2)
 */
public record GenerateResponse(
        String type,
        String value
) {}
