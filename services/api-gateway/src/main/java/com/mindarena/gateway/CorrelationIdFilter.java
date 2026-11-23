package com.mindarena.gateway;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Correlation-Id";
    private static final String SAFE_VALUE = "[A-Za-z0-9._-]{1,64}";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String supplied = exchange.getRequest().getHeaders().getFirst(HEADER);
        String correlationId = supplied != null && supplied.matches(SAFE_VALUE)
                ? supplied
                : UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set(HEADER, correlationId);
        var request = exchange.getRequest().mutate().headers(headers -> {
            headers.remove(HEADER);
            headers.set(HEADER, correlationId);
        }).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
