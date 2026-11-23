package com.mindarena.identity.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class ServiceTokenAuthorizationManagerTest {

    private final ServiceTokenAuthorizationManager manager = new ServiceTokenAuthorizationManager(
            "mindarena-identity-service",
            "mindarena-submission-service",
            "identity:user:verify"
    );

    @Test
    void exactCallerAudienceAndScopeAreRequired() {
        assertThat(decide("mindarena-submission-service", "mindarena-identity-service", "identity:user:verify", "service"))
                .isTrue();
        assertThat(decide("mindarena-submission-service", "mindarena-challenge-service", "identity:user:verify", "service"))
                .isFalse();
        assertThat(decide("mindarena-submission-service", "mindarena-identity-service", "other:scope", "service"))
                .isFalse();
        assertThat(decide("mindarena-ranking-service", "mindarena-identity-service", "identity:user:verify", "service"))
                .isFalse();
        assertThat(decide("mindarena-submission-service", "mindarena-identity-service", "identity:user:verify", "user"))
                .isFalse();
    }

    private boolean decide(String subject, String audience, String scope, String tokenUse) {
        Instant now = Instant.now();
        Jwt jwt = new Jwt(
                "token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", subject, "aud", List.of(audience), "token_use", tokenUse)
        );
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("SCOPE_" + scope))
        );
        return manager.check(() -> authentication, new RequestAuthorizationContext(null)).isGranted();
    }
}
