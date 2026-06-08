package com.mindarena.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 80)
        String fullName,
        @NotBlank @Email @Size(max = 255)
        String email,
        @NotBlank @Size(min = 6, max = 255)
        String password,
        @Size(max = 255)
        String skills,
        @Size(max = 255)
        String interests
) {
}
