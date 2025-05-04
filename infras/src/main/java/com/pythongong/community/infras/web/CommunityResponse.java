package com.pythongong.community.infras.web;

import javax.naming.CommunicationException;

import reactor.core.publisher.Mono;

public class CommunityResponse {

    public static final int SUCESS_CODE = 200;

    public static <T> Mono<T> ok(T response) {
        return Mono.just(response);
    }

    public static <T> Mono<T> error(CommunicationException communicationException) {
        return Mono.error(communicationException);
    }
}
