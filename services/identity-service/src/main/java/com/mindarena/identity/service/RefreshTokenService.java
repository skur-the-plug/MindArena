package com.mindarena.identity.service;

import com.mindarena.identity.model.RefreshToken;
import com.mindarena.identity.model.User;
import com.mindarena.identity.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${mindarena.jwt.refresh-expiration-days:30}") long expirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationDays = expirationDays;
    }

    @Transactional
    public String issue(User user) {
        String token = randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(token));
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional
    public User rotate(String token) {
        RefreshToken refreshToken = requireActive(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getUser();
    }

    @Transactional
    public void revoke(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByTokenHash(hash(token));
        refreshToken.ifPresent(value -> {
            value.setRevoked(true);
            refreshTokenRepository.save(value);
        });
    }

    private RefreshToken requireActive(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        return refreshToken;
    }

    private String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash refresh token", ex);
        }
    }
}
