package com.pythongong.community.user.repo;

import com.pythongong.community.user.model.CommunityUser;

import reactor.core.publisher.Mono;

public interface CustomCommunityUserRepo {
    Mono<Long> selectCountByUserName(String userName);

    Mono<CommunityUser> selectOneByUserName(String userName);
}
