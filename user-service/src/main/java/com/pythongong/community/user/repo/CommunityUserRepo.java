package com.pythongong.community.user.repo;

import com.pythongong.community.user.domain.CommunityUser;

import reactor.core.publisher.Mono;

public interface CommunityUserRepo {

    Mono<Integer> selectCountByUserName(String userName);

    Mono<? extends Void> insert(CommunityUser communityUser);

    // void insert(CommunityUserRecord userRecord);
}
