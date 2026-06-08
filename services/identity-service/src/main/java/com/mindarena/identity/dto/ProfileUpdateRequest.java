package com.mindarena.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 80)
        String fullName,
        @Size(max = 255)
        String skills,
        @Size(max = 255)
        String interests,
        @Size(max = 500)
        String profileImageUrl
) {
}
