package com.pythongong.community.user.repo.impl;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.pythongong.community.user.repo.CommunityUserRepo;

import reactor.core.publisher.Mono;

@Repository
public class CommunityUserRepoImpl implements CommunityUserRepo {

    @Autowired
    private DSLContext dslContext;

    @Override
    public Mono<Integer> selectCountByUserName(String userName) {
        // return Mono.from(dslContext.selectCount()
        // .from(COMMUNITY_USER)
        // .where(COMMUNITY_USER.USER_NAME.eq(userName)))
        // .map(record -> record.into(Integer.class));
        return null;
    }

}
