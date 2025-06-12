package com.pythongong.community.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pythongong.community.infras.exception.CommunityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1) // Ensure this handler is prioritized over default handlers
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(throwable); // If response is already committed, pass through
        }

        if (throwable instanceof CommunityException) {
            CommunityException communityException = (CommunityException) throwable;
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", communityException.getMessage());
            responseBody.put("code", communityException.getCode());

            try {
                byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            } catch (JsonProcessingException e) {
                log.error("Error serializing CommunityException to JSON", e);
                // Fallback to a simpler error response if JSON serialization fails
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
                DataBuffer buffer = response.bufferFactory().wrap("Internal Server Error".getBytes());
                return response.writeWith(Mono.just(buffer));
            }
        }

        // If it's not a CommunityException, let other handlers deal with it
        return Mono.error(throwable);
    }
}