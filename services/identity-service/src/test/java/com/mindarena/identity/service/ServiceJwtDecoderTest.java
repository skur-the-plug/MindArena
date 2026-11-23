package com.mindarena.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mindarena.identity.model.User;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

class ServiceJwtDecoderTest {

    @Test
    void acceptsValidServiceTokenAndRejectsConfusedOrUntrustedTokens() {
        KeyPair trusted = keyPair();
        JwtDecoder decoder = decoder(trusted);
        JwtService validIssuer = jwtService(trusted, 120, "mindarena-identity-service");
        String valid = validIssuer.generateServiceToken(
                "mindarena-submission-service",
                "mindarena-identity-service",
                Set.of("identity:user:verify")
        );

        assertThat(decoder.decode(valid).getSubject()).isEqualTo("mindarena-submission-service");
        assertThatThrownBy(() -> decoder.decode("not-a-jwt")).isInstanceOf(JwtException.class);

        String expired = jwtService(trusted, -1, "mindarena-identity-service").generateServiceToken(
                "mindarena-submission-service",
                "mindarena-identity-service",
                Set.of("identity:user:verify")
        );
        assertThatThrownBy(() -> decoder.decode(expired)).isInstanceOf(JwtException.class);

        String wrongIssuer = jwtService(trusted, 120, "untrusted-issuer").generateServiceToken(
                "mindarena-submission-service",
                "mindarena-identity-service",
                Set.of("identity:user:verify")
        );
        assertThatThrownBy(() -> decoder.decode(wrongIssuer)).isInstanceOf(JwtException.class);

        KeyPair attacker = keyPair();
        String forged = jwtService(attacker, 120, "mindarena-identity-service").generateServiceToken(
                "mindarena-submission-service",
                "mindarena-identity-service",
                Set.of("identity:user:verify")
        );
        assertThatThrownBy(() -> decoder.decode(forged)).isInstanceOf(JwtException.class);

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setEmail("user@example.com");
        String userToken = validIssuer.generateToken(user);
        assertThatThrownBy(() -> decoder.decode(userToken)).isInstanceOf(JwtException.class);
    }

    private JwtDecoder decoder(KeyPair keyPair) {
        RsaKeyService keyService = new RsaKeyService(
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        );
        return new SecurityConfig().serviceJwtDecoder(keyService);
    }

    private JwtService jwtService(KeyPair keyPair, long serviceExpiration, String issuer) {
        return new JwtService(keyPair.getPrivate(), keyPair.getPublic(), 3600, serviceExpiration, issuer);
    }

    private KeyPair keyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
