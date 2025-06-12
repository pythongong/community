package com.pythongong.community.gateway.filter;

import com.pythongong.community.gateway.util.GatewayExceptions;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.exception.ExcepInfo;
import com.pythongong.community.infras.web.AuthUserContext;
import com.pythongong.community.infras.web.AuthUserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    private final SecretKey jwtSecretKey;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Pattern AUTH_URL = Pattern.compile("^(?![^ ]*/erp[^ ]*/login)(.*/(auth|erp)/.*)$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if the request is for the login endpoint
        String path = exchange.getRequest().getPath().value();
        Matcher matcher = AUTH_URL.matcher(path);
        if (!matcher.matches()) {
            return chain.filter(exchange);
        }

        // Check if the request has Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(new CommunityException(new ExcepInfo("Invalid Authorization header format",
                    ExcepInfo.DEFALUT_CODE)));
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate JWT token
            Claims claims = Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            AuthUserInfo userInfo = new AuthUserInfo(Long.valueOf(claims.getSubject()), claims.get("userType",
                    String.class));
            return chain.filter(exchange)
                    .contextWrite(AuthUserContext.setContext(userInfo));
        } catch (ExpiredJwtException e) {
            return Mono.error(GatewayExceptions.EXPIRED_JWT);
        }

    }

    private Mono<Void> onError(ServerWebExchange exchange, String message,
            HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    // public static void main(String[] args) {
    // // Regular expression
    // String regex = "^(?![^ ]*/erp[^ ]*/login)(.*/(auth|erp)/.*)$";

    // // Test cases
    // String[] urls = {
    // "/auth/login",
    // "/erp/data",
    // "/hr/erp/login",
    // "/users",
    // "/erp/auth/login",
    // "/api/someotherpath",
    // "hr/erp/xlogin",
    // "hr/xerp/xlogin"
    // };

    // // Check each URL
    // for (String url : urls) {
    // if (Pattern.matches(regex, url)) {
    // System.out.println(url + " → Match");
    // } else {
    // System.out.println(url + " → No Match");
    // }
    // }
    // }

}