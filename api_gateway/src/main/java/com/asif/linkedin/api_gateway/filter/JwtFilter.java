package com.asif.linkedin.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

/**
 * JWT Authentication Filter
 *
 * Applied to all routes except for /api/v1/auth/**
 *
 * FLOW:
 * 1. Extract JWT from Authorization header
 * 2. Validate JWT signature and expiration
 * 3. Extract userId from JWT token claims
 * 4. Set userId in request header for downstream services
 * 5. Forward request to downstream service
 *
 * If JWT is invalid or missing, return 401 Unauthorized
 */
@Slf4j
@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    @Value("${jwt.secret-key}")
    private String secretKey;

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Implement JWT validation logic here
            // For example:
            // 1. Extract JWT from Authorization header
            // 2. Validate JWT signature and expiration
            // 3. Extract userId from JWT token claims
            // 4. Set userId in request header for downstream services
            // 5. Forward request to downstream service
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                log.warn("Missing or invalid Authorization header");
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);
            try{
                Claims claims = extractClaims(token);
                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();

                log.info("JWT validated for userId: {}, email: {}", userId, email);

                //Add userId to request header for downstream services
                ServerWebExchange serverWebExchange = exchange.mutate()
                        .request(request -> request.header("X-user-Id", userId)
                                .header("X-user-email", email))
                        .build();
                return chain.filter(serverWebExchange);

            } catch (Exception e) {
                log.error("Error occurred during JWT authentication: {}", e.getMessage());
                return unauthorized(exchange);
            }

        };
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Put configuration properties here if needed
    }
}
