package com.mindarena.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mindarena.identity.model.User;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
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
        assertThat(claims.get("iss")).isEqualTo("mindarena-identity-service");
        assertThat(((Collection<?>) claims.get("aud")).stream().map(Object::toString).toList())
                .containsExactly("mindarena-api");
        assertThat(claims.get("email")).isEqualTo("service@example.com");
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(claims.get("token_use")).isEqualTo("user");
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

    @Test
    void serviceTokenContainsStrictServiceClaims() {
        KeyPair keyPair = keyPair();
        JwtService jwtService = new JwtService(
                keyPair.getPrivate(),
                keyPair.getPublic(),
                3600,
                120,
                "mindarena-identity-service"
        );

        Map<String, Object> claims = jwtService.validateToken(jwtService.generateServiceToken(
                "mindarena-submission-service",
                "mindarena-challenge-service",
                Set.of("challenge:submission:validate")
        ));

        assertThat(claims.get("iss")).isEqualTo("mindarena-identity-service");
        assertThat(claims.get("sub")).isEqualTo("mindarena-submission-service");
        assertThat(((Collection<?>) claims.get("aud")).stream().map(Object::toString).toList())
                .containsExactly("mindarena-challenge-service");
        assertThat(claims.get("scope")).isEqualTo("challenge:submission:validate");
        assertThat(claims.get("token_use")).isEqualTo("service");
        assertThat(((Number) claims.get("exp")).longValue() - ((Number) claims.get("iat")).longValue())
                .isEqualTo(120);
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
