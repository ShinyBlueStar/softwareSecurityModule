package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.annotation.ValidValidateRequest;
import com.sample.system.ssm.service.domain.enums.SecretType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for POST /card/validate. Session ID comes from X-Session-Id header only.
 */
@Getter
@Setter
@ValidValidateRequest
public class ValidateRequestCommand {
    @NotNull(message = "type must not be null")
    private SecretType type;
    private String value;
}
