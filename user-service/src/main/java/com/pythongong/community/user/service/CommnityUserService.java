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

        communityUserRepo.selectCountByUserName(userName).subscribe(value -> {
            if (value > 0) {
                Mono.error(
                        new IllegalArgumentException("User name is duplicated"));
            }
        });

    }

}
