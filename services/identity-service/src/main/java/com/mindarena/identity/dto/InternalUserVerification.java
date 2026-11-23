package com.mindarena.identity.dto;

import com.mindarena.identity.model.User;

public record InternalUserVerification(
        Long userId,
        String fullName,
        String role,
        boolean eligible
) {
    public static InternalUserVerification from(User user) {
        return new InternalUserVerification(user.getId(), user.getFullName(), user.getRole().name(), true);
    }
}
