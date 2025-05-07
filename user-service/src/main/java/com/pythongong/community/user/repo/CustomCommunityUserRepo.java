package com.pythongong.community.user.repo;

import com.pythongong.community.user.domain.CommunityUser;

import reactor.core.publisher.Mono;

public interface CustomCommunityUserRepo {
    Mono<Long> selectCountByUserName(String userName);

    Mono<CommunityUser> selectOneByUserName(String userName);
}
