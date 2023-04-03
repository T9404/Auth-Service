package com.example.messenger.validation.generalValid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AvailableCharValidator.class)
@Documented
public @interface AvailableChar {
    String message() default "Не допустимые символы";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
