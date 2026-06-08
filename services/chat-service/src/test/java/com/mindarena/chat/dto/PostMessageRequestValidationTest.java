package com.mindarena.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class PostMessageRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsUnknownRoomTypeAndBlankContent() {
        PostMessageRequest request = new PostMessageRequest("DIRECT", null, null, "");

        var violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }
}
