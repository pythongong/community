package com.pythongong.community.infras.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret:your-default-secret-key}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    @Bean
    public SecretKey secretKey() {
        return Jwts.SIG.HS256.key().build();
    }

    public Long getExpiration() {
        return expiration;
    }
}