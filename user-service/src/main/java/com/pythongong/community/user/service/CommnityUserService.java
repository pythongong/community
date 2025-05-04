package com.pythongong.community.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pythongong.community.user.enums.UserType;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.request.RegisterUserRequest;

import reactor.core.publisher.Mono;

@Service
public class CommnityUserService {

    private static final UserType DEFAUL_TYPE = UserType.REGULAR;

    @Autowired
    private CommunityUserRepo communityUserRepo;

    public void register(RegisterUserRequest registerUserRequest) {
        String userName = registerUserRequest.userName();

        communityUserRepo.selectCountByUserName(userName).flatMap(value -> {
            if (value > 0) {
                return Mono.error(
                        new IllegalArgumentException("Value must not be greater than zero. Received: " + value));
            } else {
                return Mono.empty();
            }
        });

    }

}
