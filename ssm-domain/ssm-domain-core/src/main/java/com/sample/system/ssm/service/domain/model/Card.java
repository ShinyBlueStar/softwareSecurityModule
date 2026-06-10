package com.sample.system.ssm.service.domain.model;

import com.sample.system.ssm.service.domain.enums.SecretType;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class Card {
    UUID sessionId;
    SecretType reqType;
    String value;
    UUID cardId;
    String encryptedPin;
    String hashedPin;
    String pan;
    String serviceCode;
    String expTime;
    String encryptedCvv;
    String hashedCvv;
    String encryptedOtp;
    String encryptedData;
}
