package com.example.messenger.validation.generalValid;

import jakarta.validation.ConstraintValidator;

public class AvailableCharValidator implements ConstraintValidator<AvailableChar, String> {
    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext context) {
        if (value != null && !value.isEmpty()) {
            return value.matches("^[\\w\\s\\_\\-\\.\\,\\:\\@\\#\\!\\?\\*\\<\\>\\&\\~\\`\\|\\/\\+\\=]*$");
        }
        return true;
    }
}
