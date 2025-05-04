package com.pythongong.community.user.repo;

import reactor.core.publisher.Mono;

public interface CommunityUserRepo {

    Mono<Integer> selectCountByUserName(String userName);

    // void insert(CommunityUserRecord userRecord);
}
