package com.mindarena.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mindarena.identity.model.User;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    @Test
    void generatedTokenContainsCurrentUserClaims() {
        KeyPair keyPair = keyPair();
        JwtService jwtService = new JwtService(keyPair.getPrivate(), keyPair.getPublic(), 3600);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setEmail("service@example.com");

        Map<String, Object> claims = jwtService.validateToken(jwtService.generateToken(user));

        assertThat(claims.get("sub")).isEqualTo("42");
        assertThat(claims.get("email")).isEqualTo("service@example.com");
        assertThat(claims.get("role")).isEqualTo("USER");
    }

    @Test
    void validateTokenRejectsExpiredToken() {
        KeyPair keyPair = keyPair();
        JwtService jwtService = new JwtService(keyPair.getPrivate(), keyPair.getPublic(), -1);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setEmail("service@example.com");
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token expired");
    }

    private KeyPair keyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
