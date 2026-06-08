package com.mindarena.submission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotNull
        Long challengeId,
        @Size(max = 4000)
        String content,
        @Size(max = 12000)
        String contentJson
) {
}
