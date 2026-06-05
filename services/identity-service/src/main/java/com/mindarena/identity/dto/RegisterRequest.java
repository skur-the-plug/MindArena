package com.mindarena.identity.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password,
        String skills,
        String interests
) {
}
