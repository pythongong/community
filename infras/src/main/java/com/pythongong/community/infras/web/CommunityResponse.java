package com.pythongong.community.infras.web;

import com.pythongong.community.infras.exception.CommunityException;

import reactor.core.publisher.Mono;

public record CommunityResponse(int code, Object body) {

    public static final int SUCESS_CODE = 0;

    public static <T> Mono<CommunityResponse> ok(Mono<T> monoBody) {
        return monoBody.map(val -> new CommunityResponse(SUCESS_CODE, val))
                .switchIfEmpty(Mono.just(new CommunityResponse(SUCESS_CODE, "")))
                .onErrorResume(CommunityException.class,
                        error -> Mono.just(new CommunityResponse(error.getCode(), error.getMessage())));
    }

}
