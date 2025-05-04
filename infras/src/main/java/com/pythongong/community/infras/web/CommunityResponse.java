package com.pythongong.community.infras.web;

import reactor.core.publisher.Mono;

public class CommunityResponse {

    public static <T> Mono<T> ok(T response) {
        return Mono.just(response);
    }

    public static <T> Mono<T> error(Throwable exception) {
        return Mono.error(exception);
    }
}
