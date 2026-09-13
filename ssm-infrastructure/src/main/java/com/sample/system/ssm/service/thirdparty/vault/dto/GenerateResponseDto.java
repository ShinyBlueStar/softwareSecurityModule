package com.sample.system.ssm.service.thirdparty.vault.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateResponseDto {
    // Fields will be added when needed based on vault-service response structure
}
