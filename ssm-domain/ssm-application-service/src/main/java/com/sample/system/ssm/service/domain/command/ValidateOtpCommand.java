package com.sample.system.ssm.service.domain.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ValidateOtpCommand(
    @NotNull UUID sessionId,
    @NotBlank String otp
) {}
