package com.sample.system.ssm.service.domain.annotation;

import com.sample.system.ssm.service.domain.command.ValidateRequestCommand;
import com.sample.system.ssm.service.domain.enums.SecretType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ValidateRequestValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private ValidateRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidateRequestValidator();
        var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(builder);
    }

    @Test
    void isValid_rejectsNullValue() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void isValid_acceptsCvvValue_atLeast23Chars() {
        var command = new ValidateRequestCommand();
        command.setType(SecretType.CVV);
        command.setValue("x".repeat(23));

        assertThat(validator.isValid(command, context)).isTrue();
    }

    @Test
    void isValid_rejectsCvvValue_shorterThan23Chars() {
        var command = new ValidateRequestCommand();
        command.setType(SecretType.CVV);
        command.setValue("too-short");

        assertThat(validator.isValid(command, context)).isFalse();
    }

    /**
     * Documents current (known-buggy) behavior: OTP/PIN validate requests fall through
     * to the CVV2 rule (value length >= 23) instead of the intended OTP/PIN rule, because
     * the call to validateOtpPin(...) is commented out in ValidateRequestValidator. See
     * README "Known issues". If this test starts failing after a deliberate fix, update
     * it to assert the new intended behavior instead of re-disabling it.
     */
    @Test
    void isValid_otpAndPinCurrentlyFallThroughToTheCvvLengthRule() {
        var otp = new ValidateRequestCommand();
        otp.setType(SecretType.OTP);
        otp.setValue("short");
        assertThat(validator.isValid(otp, context))
                .as("OTP with a short value is rejected by the CVV length rule, not an OTP-specific rule")
                .isFalse();

        var pin = new ValidateRequestCommand();
        pin.setType(SecretType.PIN);
        pin.setValue("x".repeat(23));
        assertThat(validator.isValid(pin, context))
                .as("PIN passes only because it happens to satisfy the CVV length rule")
                .isTrue();
    }
}
