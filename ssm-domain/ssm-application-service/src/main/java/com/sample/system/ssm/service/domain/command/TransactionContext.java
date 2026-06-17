package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.enums.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Transaction context for validation operations
 * Used for audit logging and fraud detection
 */
public record TransactionContext(
    @NotBlank String transactionId,
    @NotNull Long amount,
    @NotBlank String currency,
    String merchantId,
    @NotNull Instant timestamp,
    Channel channel
) {}

