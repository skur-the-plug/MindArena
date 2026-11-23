package com.mindarena.submission.client;

record ServiceTokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
