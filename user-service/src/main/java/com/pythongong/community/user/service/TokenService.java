package com.pythongong.community.user.service;

import javax.crypto.SecretKey;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.web.AuthUserInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final SecretKey jwtSecretKey;
    
    @Value("${jwt.access-token.expiration:300}") // 5 minutes
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:2592000}") // 30 days
    private Long refreshTokenExpiration;

    public String generateAccessToken(AuthUserInfo userInfo) {
        return generateToken(userInfo, accessTokenExpiration);
    }

    public String generateRefreshToken(AuthUserInfo userInfo) {
        return generateToken(userInfo, refreshTokenExpiration);
    }

    private String generateToken(AuthUserInfo userInfo, long expiration) {
        return Jwts.builder()
            .subject(String.valueOf(userInfo.userId()))
            .claim("userType", userInfo.userType())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(jwtSecretKey)
            .compact();
    }

    public void setRefreshTokenCookie(ServerWebExchange exchange, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshTokenExpiration)
            .sameSite("Strict")
            .build();
        exchange.getResponse().addCookie(cookie);
    }

    public Mono<String> refreshToken(ServerWebExchange exchange) {
        String refreshToken = exchange.getRequest()
        .getCookies()
        .getFirst("refresh_token")
        .getValue();

    try {
        Claims claims = Jwts.parser()
            .verifyWith(jwtSecretKey)
            .build()
            .parseSignedClaims(refreshToken)
            .getPayload();

        AuthUserInfo userInfo = new AuthUserInfo(
            Long.valueOf(claims.getSubject()),
            claims.get("userType", String.class)
        );

        String newAccessToken = generateAccessToken(userInfo);
        String newRefreshToken = generateRefreshToken(userInfo);
        
        setRefreshTokenCookie(exchange, newRefreshToken);
        return Mono.just(newAccessToken);
    } catch (Exception e) {
        throw new CommunityException("Invalid refresh token", CommunityException.UNAUTHORIZED);
    }
    }
}
