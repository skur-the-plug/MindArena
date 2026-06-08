package com.mindarena.identity.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserPayload user
) {
}
