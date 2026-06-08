package com.mindarena.identity.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidRegistrationInput() {
        RegisterRequest request = new RegisterRequest("", "not-an-email", "123", null, null);

        var violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }
}
