package com.mindarena.identity.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ServiceClientRegistryTest {

    @Test
    void authenticatesRegisteredServiceAndReturnsOnlyTargetScope() throws Exception {
        ServiceClientRegistry registry = new ServiceClientRegistry(
                "mindarena-submission-service",
                sha256("high-entropy-secret")
        );

        var grant = registry.authenticate(
                "mindarena-submission-service",
                "high-entropy-secret",
                "mindarena-identity-service"
        );

        assertThat(grant.serviceId()).isEqualTo("mindarena-submission-service");
        assertThat(grant.scopes()).containsExactly("identity:user:verify");
    }

    @Test
    void rejectsBadSecretAndUnauthorizedAudience() throws Exception {
        ServiceClientRegistry registry = new ServiceClientRegistry(
                "mindarena-submission-service",
                sha256("high-entropy-secret")
        );

        assertThatThrownBy(() -> registry.authenticate(
                "mindarena-submission-service",
                "wrong-secret",
                "mindarena-identity-service"
        )).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> registry.authenticate(
                "mindarena-submission-service",
                "high-entropy-secret",
                "mindarena-ranking-service"
        )).isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
