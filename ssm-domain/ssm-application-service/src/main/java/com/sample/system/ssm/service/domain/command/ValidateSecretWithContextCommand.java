package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.enums.SecretType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Command for validating secrets with transaction context
 */
public record ValidateSecretWithContextCommand(
    @NotNull UUID sessionId,
    @NotNull SecretType secretType,
    @NotBlank String secretValue,  // Plain PIN/OTP/CVV from customer
    String cardData,  // For CVV2: "pan|expTime|serviceCode", for PIN1: "pan" (optional)
    @Valid TransactionContext transactionContext
) {}

