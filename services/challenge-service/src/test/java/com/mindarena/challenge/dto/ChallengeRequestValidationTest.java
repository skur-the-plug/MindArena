package com.mindarena.challenge.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ChallengeRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMissingRequiredFieldsAndUnknownTemplateType() {
        ChallengeRequest request = new ChallengeRequest(null, "", "", null, "UNKNOWN", null, null, null);

        var violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
}
