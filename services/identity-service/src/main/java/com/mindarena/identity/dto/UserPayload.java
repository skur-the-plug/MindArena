package com.mindarena.identity.dto;

import com.mindarena.identity.model.User;

public record UserPayload(
        Long id,
        String fullName,
        String email,
        String skills,
        String interests,
        String profileImageUrl,
        String role,
        int score
) {
    public static UserPayload from(User user) {
        return new UserPayload(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getSkills(),
                user.getInterests(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getScore()
        );
    }
}
