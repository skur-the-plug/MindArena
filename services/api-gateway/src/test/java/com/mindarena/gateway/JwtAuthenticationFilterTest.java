package com.mindarena.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPairGenerator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    @Test
    void rejectsApiRequestsWithoutBearerToken() {
        TestJwtService jwtService = new TestJwtService(Map.of());
        TestGatewayFilterChain chain = new TestGatewayFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/challenges").build()
        );

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(jwtService.wasCalled()).isFalse();
        assertThat(chain.wasCalled()).isFalse();
    }

    @Test
    void allowsPublicAuthRoutesWithoutToken() {
        TestJwtService jwtService = new TestJwtService(Map.of());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            capturedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/identity/auth/login").build()
        );

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get()).isSameAs(exchange);
        assertThat(jwtService.wasCalled()).isFalse();
    }

    @Test
    void rejectsDiscoveryStyleServiceRoutesWithoutBearerToken() {
        TestJwtService jwtService = new TestJwtService(Map.of());
        TestGatewayFilterChain chain = new TestGatewayFilterChain();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/mindarena-challenge-service/api/challenges")
                        .header("X-User-Role", "ADMIN")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(jwtService.wasCalled()).isFalse();
        assertThat(chain.wasCalled()).isFalse();
    }

    @Test
    void injectsTrustedUserHeadersAndRemovesSpoofedValues() {
        TestJwtService jwtService = new TestJwtService(Map.of(
                "sub", "7",
                "email", "admin@example.com",
                "role", "ADMIN"
        ));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            capturedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/challenges")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                        .header("X-User-Id", "999")
                        .header("X-User-Email", "spoof@example.com")
                        .header("X-User-Role", "USER")
                        .build()
        );

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedExchange.get().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("7");
        assertThat(headers.getFirst("X-User-Email")).isEqualTo("admin@example.com");
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("ADMIN");
    }

    private static class TestJwtService extends JwtService {

        private final Map<String, Object> claims;
        private boolean called;

        private TestJwtService(Map<String, Object> claims) {
            super(testPublicKey());
            this.claims = claims;
        }

        @Override
        public Map<String, Object> validateToken(String token) {
            called = true;
            assertThat(token).isEqualTo("valid-token");
            return claims;
        }

        private boolean wasCalled() {
            return called;
        }

        private static java.security.PublicKey testPublicKey() {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                return generator.generateKeyPair().getPublic();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static class TestGatewayFilterChain implements GatewayFilterChain {

        private boolean called;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            called = true;
            return Mono.empty();
        }

        private boolean wasCalled() {
            return called;
        }
    }
}
