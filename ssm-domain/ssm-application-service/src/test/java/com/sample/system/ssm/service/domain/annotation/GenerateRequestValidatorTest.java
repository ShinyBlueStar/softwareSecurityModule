package com.sample.system.ssm.service.domain.annotation;

import com.sample.system.ssm.service.domain.command.GenerateRequestCommand;
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
class GenerateRequestValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private GenerateRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GenerateRequestValidator();
        var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(builder);
    }

    @Test
    void isValid_rejectsNullValue() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void isValid_rejectsNullType() {
        var command = new GenerateRequestCommand();
        assertThat(validator.isValid(command, context)).isFalse();
    }

    @Test
    void isValid_acceptsPinRequest_withNoExtraFields() {
        var command = commandOf(SecretType.PIN, null);
        assertThat(validator.isValid(command, context)).isTrue();
    }

    @Test
    void isValid_acceptsOtpRequest_withNoExtraFields() {
        var command = commandOf(SecretType.OTP, null);
        assertThat(validator.isValid(command, context)).isTrue();
    }

    @Test
    void isValid_rejectsCvvRequest_withoutEncryptedData() {
        var command = commandOf(SecretType.CVV, null);
        assertThat(validator.isValid(command, context)).isFalse();
    }

    @Test
    void isValid_acceptsCvvRequest_withEncryptedData() {
        var command = commandOf(SecretType.CVV, "base64-encrypted-blob");
        assertThat(validator.isValid(command, context)).isTrue();
    }

    private static GenerateRequestCommand commandOf(SecretType type, String encryptedData) {
        var command = new GenerateRequestCommand();
        setField(command, "type", type);
        setField(command, "encryptedData", encryptedData);
        return command;
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
