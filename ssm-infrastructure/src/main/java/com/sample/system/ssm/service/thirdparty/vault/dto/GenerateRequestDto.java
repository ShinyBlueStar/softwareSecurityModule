package com.sample.system.ssm.service.thirdparty.vault.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class GenerateRequestDto {
    UUID sessionId;
    String type;
    String cardNumber;
}
