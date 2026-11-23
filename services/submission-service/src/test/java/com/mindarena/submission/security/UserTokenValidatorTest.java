package com.mindarena.submission.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class UserTokenValidatorTest {

    private final UserTokenValidator validator = new UserTokenValidator();

    @Test
    void acceptsOnlyUserTokenForApiAudience() {
        assertThat(validator.validate(jwt("user", "mindarena-api")).hasErrors()).isFalse();
        assertThat(validator.validate(jwt("service", "mindarena-api")).hasErrors()).isTrue();
        assertThat(validator.validate(jwt("user", "mindarena-identity-service")).hasErrors()).isTrue();
    }

    private Jwt jwt(String tokenUse, String audience) {
        Instant now = Instant.now();
        return new Jwt(
                "token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("sub", "7", "aud", List.of(audience), "token_use", tokenUse)
        );
    }
}
