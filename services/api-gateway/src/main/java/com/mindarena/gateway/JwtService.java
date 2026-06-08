package com.mindarena.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Map;
import java.security.PublicKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final PublicKey publicKey;

    public JwtService(RsaKeyService keyService) {
        this.publicKey = keyService.publicKey();
    }

    JwtService(PublicKey publicKey) {
        this.publicKey = publicKey;
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
