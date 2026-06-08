package com.mindarena.identity.service;

import com.mindarena.identity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expirationSeconds;

    public JwtService(
            RsaKeyService keyService,
            @Value("${mindarena.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.privateKey = keyService.privateKey();
        this.publicKey = keyService.publicKey();
        this.expirationSeconds = expirationSeconds;
    }

    JwtService(PrivateKey privateKey, PublicKey publicKey, long expirationSeconds) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.expirationSeconds = expirationSeconds;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims;
        } catch (ExpiredJwtException ex) {
            throw new IllegalArgumentException("Token expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid token", ex);
        }
    }
}
