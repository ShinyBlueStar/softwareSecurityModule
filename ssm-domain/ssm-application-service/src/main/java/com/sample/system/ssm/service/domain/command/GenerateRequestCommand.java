package com.sample.system.ssm.service.domain.command;

import com.sample.system.ssm.service.domain.annotation.ValidGenerateRequest;
import com.sample.system.ssm.service.domain.enums.SecretType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@ValidGenerateRequest
public class GenerateRequestCommand {

    @NotNull(message = "type must not be null")
    private SecretType type;

    private String encryptedData;

}
