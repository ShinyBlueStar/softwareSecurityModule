package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to create a new SSM session for card operations")
public record CreateSessionCommand(
    @NotNull
    @Schema(description = "Card UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID cardId,
    @NotNull
    @Schema(description = "Channel: WEB, MOBILE, API, POS, ATM", example = "WEB")
    Channel channel,
    @NotBlank
    @Schema(description = "Session type: e.g. CARD_ACTIVATION, PIN_CHANGE, TRANSACTION", example = "CARD_ACTIVATION")
    String sessionType,
    @Schema(description = "Optional device/browser fingerprint for audit", example = "fp_a1b2c3d4e5f6_hash_xyz")
    String clientFingerprint
) {}
