package com.pythongong.community.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pythongong.community.infras.web.CommunityResponse;
import com.pythongong.community.user.request.RegisterUserRequest;
import com.pythongong.community.user.service.CommnityUserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class CommunityUserController {

    @Autowired
    private CommnityUserService commnityUserService;

    @PostMapping("/register")
    public Mono<CommunityResponse> register(@RequestBody RegisterUserRequest request) {
        return CommunityResponse.ok(commnityUserService.register(request));
    }

}
