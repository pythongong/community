package com.pythongong.community.infras.filter;

import javax.crypto.SecretKey;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.web.AuthUserContext;
import com.pythongong.community.infras.web.AuthUserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final SecretKey jwtSecretKey;

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(new CommunityException("Invalid or missing JWT token", CommunityException.UNAUTHORIZED));
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Add user info to the exchange attributes
            AuthUserContext.set(
                    new AuthUserInfo(Integer.valueOf(claims.getSubject()), claims.get("userType", String.class)));
            return chain.filter(exchange);
        } catch (Exception e) {
            return Mono.error(new CommunityException("Invalid JWT token", CommunityException.UNAUTHORIZED));
        }
    }
}