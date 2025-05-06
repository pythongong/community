package com.pythongong.community.user.repo;

import com.pythongong.community.user.domain.CommunityUser;

import reactor.core.publisher.Mono;

public interface CommunityUserRepo {

    Mono<Long> selectCountByUserName(String userName);

    Mono<Long> insert(CommunityUser communityUser);

    // void insert(CommunityUserRecord userRecord);
}
