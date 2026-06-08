package com.mindarena.gateway;

import java.util.Map;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        if (!isProtectedPath(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        try {
            Map<String, Object> claims = jwtService.validateToken(authorization.substring(BEARER_PREFIX.length()));
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Role");
                    })
                    .header("X-User-Id", claims.get("sub").toString())
                    .header("X-User-Email", claims.get("email").toString())
                    .header("X-User-Role", claims.get("role").toString())
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (RuntimeException ex) {
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/identity/auth/register")
                || path.equals("/api/identity/auth/login")
                || path.equals("/api/identity/auth/refresh")
                || path.equals("/api/identity/auth/logout")
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/health/");
    }

    private boolean isProtectedPath(String path) {
        return path.startsWith("/api/")
                || path.startsWith("/mindarena-");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
