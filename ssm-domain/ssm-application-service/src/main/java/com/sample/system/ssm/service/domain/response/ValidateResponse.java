package com.sample.system.ssm.service.domain.response;

/**
 * Response for validate/verify operations (PIN/OTP/CVV2)
 */
public record ValidateResponse(
        boolean valid,
        String message
) {}
