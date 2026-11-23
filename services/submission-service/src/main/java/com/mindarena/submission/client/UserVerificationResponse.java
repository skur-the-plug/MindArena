package com.mindarena.submission.client;

public record UserVerificationResponse(Long userId, String fullName, String role, boolean eligible) {
}
