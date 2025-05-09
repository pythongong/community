package com.pythongong.community.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.pythongong.community.infras.util.WebUtil;
import com.pythongong.community.infras.web.CommunityResponse;
import com.pythongong.community.user.request.LoginUserRequest;
import com.pythongong.community.user.request.RegisterUserRequest;
import com.pythongong.community.user.service.CommnityUserService;
import com.pythongong.community.user.service.TokenService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(WebUtil.FRONT_PATH + "/user")
@RequiredArgsConstructor
public class CommunityUserController {

    private final CommnityUserService commnityUserService;

    private final TokenService tokenService;

    @PostMapping("/register")
    public Mono<CommunityResponse> register(@RequestBody RegisterUserRequest request) {
        return CommunityResponse.ok(commnityUserService.register(request));
    }

    @PostMapping("/login")
    public Mono<CommunityResponse> login(@RequestBody LoginUserRequest request, ServerWebExchange exchange) {
        return CommunityResponse.ok(commnityUserService.login(request, exchange));
    }

    @PostMapping("/refresh-token")
    public Mono<CommunityResponse> refreshToken(ServerWebExchange exchange) {

        return CommunityResponse.ok(tokenService.refreshToken(exchange));
    }
}
