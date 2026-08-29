package com.sample.system.ssm.service.domain.mapper;

import com.sample.system.ssm.service.domain.command.GenerateRequestCommand;
import com.sample.system.ssm.service.domain.command.ValidateRequestCommand;
import com.sample.system.ssm.service.domain.enums.SecretType;
import com.sample.system.ssm.service.domain.model.Card;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CardDataMapperTest {

    @Test
    void toModel_fromValidateRequest_populatesEncryptedOtp_onlyForOtpType() {
        UUID sessionId = UUID.randomUUID();
        var req = new ValidateRequestCommand();
        req.setType(SecretType.OTP);
        req.setValue("encrypted-otp-value");

        Card card = CardDataMapper.toModel(req, sessionId);

        assertThat(card.getSessionId()).isEqualTo(sessionId);
        assertThat(card.getReqType()).isEqualTo(SecretType.OTP);
        assertThat(card.getValue()).isEqualTo("encrypted-otp-value");
        assertThat(card.getEncryptedOtp()).isEqualTo("encrypted-otp-value");
    }

    @Test
    void toModel_fromValidateRequest_leavesEncryptedOtpNull_forNonOtpTypes() {
        var req = new ValidateRequestCommand();
        req.setType(SecretType.PIN);
        req.setValue("encrypted-pin-value");

        Card card = CardDataMapper.toModel(req, UUID.randomUUID());

        assertThat(card.getEncryptedOtp()).isNull();
    }

    @Test
    void toModel_fromValidateRequest_returnsNull_forNullRequest() {
        assertThat(CardDataMapper.toModel((ValidateRequestCommand) null, UUID.randomUUID())).isNull();
    }

    @Test
    void toModel_fromGenerateRequest_carriesEncryptedDataThrough() {
        UUID sessionId = UUID.randomUUID();
        var req = new GenerateRequestCommand();
        setField(req, "type", SecretType.CVV);
        setField(req, "encryptedData", "base64-blob");

        Card card = CardDataMapper.toModel(req, sessionId);

        assertThat(card.getSessionId()).isEqualTo(sessionId);
        assertThat(card.getReqType()).isEqualTo(SecretType.CVV);
        assertThat(card.getEncryptedData()).isEqualTo("base64-blob");
    }

    @Test
    void toVerifyResponse_isValidOnlyWhenCardValueIsExactlyValid() {
        Card valid = new Card();
        valid.setValue("valid");
        assertThat(CardDataMapper.toVerifyResponse(valid).valid()).isTrue();

        Card invalid = new Card();
        invalid.setValue("invalid");
        assertThat(CardDataMapper.toVerifyResponse(invalid).valid()).isFalse();

        assertThat(CardDataMapper.toVerifyResponse(null).valid()).isFalse();
    }

    @Test
    void toGenerateResponse_mapsTypeAndValue() {
        Card card = new Card();
        card.setReqType(SecretType.PIN);
        card.setValue("encrypted-pin");

        var response = CardDataMapper.toGenerateResponse(card);

        assertThat(response.type()).isEqualTo("PIN");
        assertThat(response.value()).isEqualTo("encrypted-pin");
    }

    @Test
    void toGenerateResponse_returnsEmptyResponse_forNullCard() {
        var response = CardDataMapper.toGenerateResponse(null);

        assertThat(response.type()).isNull();
        assertThat(response.value()).isNull();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
