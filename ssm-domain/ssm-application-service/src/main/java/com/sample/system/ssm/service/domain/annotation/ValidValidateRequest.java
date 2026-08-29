package com.sample.system.ssm.service.domain.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateRequestValidator.class)
@Documented
public @interface ValidValidateRequest {
    String message() default "Invalid validate request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
