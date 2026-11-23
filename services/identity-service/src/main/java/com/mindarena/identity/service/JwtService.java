package com.mindarena.identity.service;

import com.mindarena.identity.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expirationSeconds;
    private final long serviceExpirationSeconds;
    private final String issuer;

    @Autowired
    public JwtService(
            RsaKeyService keyService,
            @Value("${mindarena.jwt.expiration-seconds:86400}") long expirationSeconds,
            @Value("${mindarena.jwt.service-expiration-seconds:120}") long serviceExpirationSeconds,
            @Value("${mindarena.jwt.issuer:mindarena-identity-service}") String issuer
    ) {
        this.privateKey = keyService.privateKey();
        this.publicKey = keyService.publicKey();
        this.expirationSeconds = expirationSeconds;
        this.serviceExpirationSeconds = serviceExpirationSeconds;
        this.issuer = issuer;
    }

    JwtService(PrivateKey privateKey, PublicKey publicKey, long expirationSeconds) {
        this(privateKey, publicKey, expirationSeconds, 120, "mindarena-identity-service");
    }

    JwtService(
            PrivateKey privateKey,
            PublicKey publicKey,
            long expirationSeconds,
            long serviceExpirationSeconds,
            String issuer
    ) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.expirationSeconds = expirationSeconds;
        this.serviceExpirationSeconds = serviceExpirationSeconds;
        this.issuer = issuer;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("aud", "mindarena-api")
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("token_use", "user")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateServiceToken(String serviceId, String audience, Set<String> scopes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(serviceId)
                .claim("aud", audience)
                .claim("scope", String.join(" ", scopes))
                .claim("token_use", "service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(serviceExpirationSeconds)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public long serviceExpirationSeconds() {
        return serviceExpirationSeconds;
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
