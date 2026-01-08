package com.innowise.gateway.filter;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final String jwtSecret;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "JWT token missing", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return chain.filter(exchange);
        } catch (Exception e) {
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String jsonResponse = String.format(
                "{\"error\":\"%s\",\"status\":%d}", error, status.value()
        );
        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
