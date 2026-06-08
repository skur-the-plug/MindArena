package com.mindarena.submission.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class SubmissionRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMissingChallengeIdAndBlankComment() {
        assertThat(validator.validate(new SubmissionRequest(null, null, null))).isNotEmpty();
        assertThat(validator.validate(new CommentRequest(""))).isNotEmpty();
    }
}
