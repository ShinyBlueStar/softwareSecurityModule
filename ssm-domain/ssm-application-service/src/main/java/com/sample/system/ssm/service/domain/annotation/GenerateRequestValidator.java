package com.sample.system.ssm.service.domain.annotation;

import com.sample.system.ssm.service.domain.command.GenerateRequestCommand;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class GenerateRequestValidator
        implements ConstraintValidator<ValidGenerateRequest, GenerateRequestCommand> {

    @Override
    public boolean isValid(GenerateRequestCommand value,
                           ConstraintValidatorContext context) {

        if (value == null || value.getType() == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        return switch (value.getType()) {
            case OTP, PIN -> validateOtpPin(value, context);
            case CVV -> validateCvv(value, context);
            default -> false;
        };
    }

    private boolean validateOtpPin(GenerateRequestCommand value,
                                   ConstraintValidatorContext context) {

        if (isAnyFieldPresent(value)) {
            context.buildConstraintViolationWithTemplate(
                    "No additional fields allowed for OTP or PIN")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateCvv(GenerateRequestCommand value,
                                ConstraintValidatorContext context) {

        boolean valid = true;

        if (isBlank(value.getEncryptedData())) {
            addViolation(context, "encryptedData is required for CVV");
            valid = false;
        }

        return valid;
    }

    private boolean isAnyFieldPresent(GenerateRequestCommand value) {
        return value.getEncryptedData() != null;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
