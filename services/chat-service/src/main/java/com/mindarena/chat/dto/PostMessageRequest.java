package com.mindarena.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostMessageRequest(
        @NotBlank @Pattern(regexp = "GLOBAL|ARENA|CHALLENGE")
        String roomType,
        Long arenaId,
        Long challengeId,
        @NotBlank @Size(max = 1000)
        String content
) {
}
