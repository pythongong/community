package com.pythongong.community.gateway.filter;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.web.AuthUserContext;
import com.pythongong.community.infras.web.AuthUserInfo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Mock
        private GatewayFilterChain filterChain;

        private SecretKey jwtSecretKey;

        // A sample secret key (ensure it's strong enough for production)
        private final String secretString = "TestSecretKeyForJwtValidation12345678901234567890";

        @BeforeEach
        void setUp() {
                jwtSecretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
                jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtSecretKey);
        }

        private String createToken(String subject, String userType, Date expirationDate) {
                Map<String, Object> claims = new HashMap<>();
                claims.put("userType", userType);
                return Jwts.builder()
                                .subject(subject)
                                .claims(claims)
                                .issuedAt(new Date())
                                .expiration(expirationDate)
                                .signWith(jwtSecretKey)
                                .compact();
        }

        private String createValidToken(String subject, String userType) {
                return createToken(subject, userType, new Date(System.currentTimeMillis() + 3600000)); // Expires in 1
                                                                                                       // hour
        }

        private String createExpiredToken(String subject, String userType) {
                return createToken(subject, userType, new Date(System.currentTimeMillis() - 3600000)); // Expired 1 hour
                                                                                                       // ago
        }

        @Test
        void filter_whenPathDoesNotMatchAuthUrl_shouldChainFilter() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/someotherpath").build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                // Update mock to verify context state for this specific scenario
                when(filterChain.filter(any(ServerWebExchange.class)))
                                .thenReturn(Mono.deferContextual(contextView -> {
                                        assertFalse(contextView.hasKey(AuthUserContext.AUTH_USER_INFO_KEY),
                                                        "AuthUserInfo should NOT be in context for non-matching paths");
                                        return Mono.empty();
                                }));

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(filterChain).filter(exchange);
                // AuthUserContext.get() now returns Mono<AuthUserInfo>, direct null check is
                // invalid.
                // Verification of context absence is handled in the mocked filterChain.
        }

        @Test
        void filter_whenPathMatchesAuthUrlAndNoAuthHeader_shouldReturnUnauthorized() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login").build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
                // AuthUserContext.get() now returns Mono<AuthUserInfo>.
                // If an error occurs before context is set, it naturally wouldn't be there.
        }

        @Test
        void filter_whenPathMatchesErpUrlAndNoAuthHeader_shouldReturnUnauthorized() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/erp/data").build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void filter_whenAuthHeaderIsInvalidFormat_shouldReturnError() {
                MockServerHttpRequest request = MockServerHttpRequest.get("/auth/login")
                                .header(HttpHeaders.AUTHORIZATION, "InvalidTokenFormat")
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .expectErrorSatisfies(throwable -> {
                                        assertInstanceOf(CommunityException.class, throwable);
                                        CommunityException ex = (CommunityException) throwable;
                                        assertEquals("Invalid Authorization header format", ex.getMessage());
                                })
                                .verify();
                // AuthUserContext.get() now returns Mono<AuthUserInfo>.
                // Context would not be populated in an error scenario like this.
        }

        @Test
        void filter_whenTokenIsValid_shouldSetAuthContextAndChainFilter() {
                String token = createValidToken("123", "USER");
                long expectedUserId = 123L;
                String expectedUserType = "USER";

                MockServerHttpRequest request = MockServerHttpRequest.get("/auth/resource")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                // Mock the chain to verify context
                when(filterChain.filter(any(ServerWebExchange.class)))
                                .thenReturn(Mono.deferContextual(contextView -> {
                                        assertTrue(contextView.hasKey(AuthUserContext.AUTH_USER_INFO_KEY),
                                                        "AuthUserInfo should be in context");
                                        AuthUserInfo userInfoFromContext = contextView
                                                        .get(AuthUserContext.AUTH_USER_INFO_KEY);
                                        assertNotNull(userInfoFromContext,
                                                        "AuthUserInfo retrieved from context should not be null");
                                        assertEquals(expectedUserId, userInfoFromContext.userId());
                                        assertEquals(expectedUserType, userInfoFromContext.userType());
                                        return Mono.empty(); // Simulate successful chain completion
                                }));

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .verifyComplete();

                verify(filterChain).filter(exchange); // Ensure the chain was called
        }

        @Test
        void filter_whenTokenIsExpired_shouldReturnError() {
                String token = createExpiredToken("123", "USER");
                MockServerHttpRequest request = MockServerHttpRequest.get("/erp/secure")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .expectErrorSatisfies(throwable -> {
                                        assertInstanceOf(CommunityException.class, throwable);
                                })
                                .verify();
                // AuthUserContext.get() now returns Mono<AuthUserInfo>.
                // Context would not be populated in an error scenario.
        }

        @Test
        void filter_whenTokenIsMalformed_shouldReturnError() {
                String malformedToken = "this.is.not.a.valid.token";
                MockServerHttpRequest request = MockServerHttpRequest.get("/auth/anotherresource")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .expectError(io.jsonwebtoken.MalformedJwtException.class) // Jwts.parser() throws this
                                .verify();
                // AuthUserContext.get() now returns Mono<AuthUserInfo>.
                // Context would not be populated in an error scenario.
        }

        @Test
        void filter_whenTokenSignatureIsInvalid_shouldReturnError() {
                SecretKey anotherKey = Keys
                                .hmacShaKeyFor("AnotherSecretKeyForJwtValidation5678901234567890123"
                                                .getBytes(StandardCharsets.UTF_8));
                String tokenWithWrongSignature = Jwts.builder()
                                .subject("123")
                                .claim("userType", "USER")
                                .expiration(new Date(System.currentTimeMillis() + 3600000))
                                .signWith(anotherKey)
                                .compact();

                MockServerHttpRequest request = MockServerHttpRequest.get("/auth/resource")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithWrongSignature)
                                .build();
                ServerWebExchange exchange = MockServerWebExchange.from(request);

                StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
                                .expectError(io.jsonwebtoken.security.SignatureException.class) // Jwts.parser() throws
                                                                                                // this
                                .verify();
                // AuthUserContext.get() now returns Mono<AuthUserInfo>.
                // Context would not be populated in an error scenario.
        }

}