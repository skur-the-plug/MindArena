package com.mindarena.notification.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CreateNotificationRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMissingRecipientUnknownTypeAndBlankMessage() {
        CreateNotificationRequest request = new CreateNotificationRequest(null, "EMAIL", "", null);

        var violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
    }
}
