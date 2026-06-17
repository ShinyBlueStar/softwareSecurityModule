package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.enums.SecretType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Body for GET /card/secrets/view. Session ID comes from X-Session-Id header only. */
public record ViewCardSecretCommand(
    @NotNull SecretType secretType,
    UUID cardId  // optional; if null, card is resolved from session
) {}

