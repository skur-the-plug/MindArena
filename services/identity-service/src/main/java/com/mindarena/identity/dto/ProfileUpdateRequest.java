package com.mindarena.identity.dto;

public record ProfileUpdateRequest(
        String fullName,
        String skills,
        String interests,
        String profileImageUrl
) {
}
