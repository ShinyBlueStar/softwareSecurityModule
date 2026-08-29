package com.sample.system.ssm.service.domain.mapper;

import com.sample.system.ssm.service.domain.command.GenerateRequestCommand;
import com.sample.system.ssm.service.domain.command.ValidateRequestCommand;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.Card;
import com.sample.system.ssm.service.domain.response.GenerateResponse;
import com.sample.system.ssm.service.domain.response.ValidateResponse;

import java.util.UUID;

/**
 * Mapper for converting between Command, Model, and Response objects.
 * Session ID is taken from X-Session-Id header (passed as sessionId param), not from request body.
 */
public class CardDataMapper {

    public static Card toModel(ValidateRequestCommand req, UUID sessionId) {
        if (req == null) {
            return null;
        }
        Card card = new Card();
        card.setSessionId(sessionId);
        card.setReqType(req.getType());
        card.setValue(req.getValue());
        if (SecretType.OTP.equals(req.getType()))
            card.setEncryptedOtp(req.getValue());
        return card;
    }

    public static Card toModel(GenerateRequestCommand req, UUID sessionId) {
        if (req == null) {
            return null;
        }
        Card card = new Card();
        card.setSessionId(sessionId);
        card.setReqType(req.getType());
        card.setEncryptedData(req.getEncryptedData());
        return card;
    }

    public static ValidateResponse toVerifyResponse(Card card) {
        if (card == null) {
            return new ValidateResponse(false, "Verification failed");
        }
        
        // Card.value contains "valid" or "invalid" after verification
        boolean isValid = "valid".equalsIgnoreCase(card.getValue());
        String message = isValid ? "Verification succeeded" : "Verification failed";
        return new ValidateResponse(isValid, message);
    }

    public static GenerateResponse toGenerateResponse(Card card) {
        if (card == null) {
            return new GenerateResponse(null, null);
        }
        
        // Card.value contains the generated value (PIN/OTP/CVV2)
        return new GenerateResponse(card.getReqType().name(), card.getValue());
    }
}
