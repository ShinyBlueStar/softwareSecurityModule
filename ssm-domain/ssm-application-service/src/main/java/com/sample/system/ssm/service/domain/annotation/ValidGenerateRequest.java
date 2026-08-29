package com.sample.system.ssm.service.domain.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenerateRequestValidator.class)
@Documented
public @interface ValidGenerateRequest {
    String message() default "Invalid generate request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
