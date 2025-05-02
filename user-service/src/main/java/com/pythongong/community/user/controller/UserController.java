package com.pythongong.community.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/hello")
    public Mono<String> handle() {
        return Mono.just("Hello WebFlux");
    }

}
