package com.mindarena.identity.dto;

public record ServiceTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
