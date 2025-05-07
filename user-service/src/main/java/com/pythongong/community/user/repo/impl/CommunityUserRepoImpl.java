package com.pythongong.community.user.repo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.repo.CustomCommunityUserRepo;

import reactor.core.publisher.Mono;

@Repository
public class CommunityUserRepoImpl implements CustomCommunityUserRepo {

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Long> selectCountByUserName(String userName) {
        return entityTemplate.count(Query.query(Criteria.where(CommunityUser.USER_NAME).is(userName)), CommunityUser.class);
        
    } 

    @Override
    public Mono<CommunityUser> selectOneByUserName(String userName) {
        return entityTemplate.select(Query.query(Criteria.where(CommunityUser.USER_NAME).is(userName)), CommunityUser.class)
                .collectList()
                .flatMap(users -> {
                    if (users.size() > 1) {
                        throw new CommunityException("Found more than one user with username: " + userName, CommunityException.COMMON_ERROR);
                    }
                    return users.isEmpty() ? Mono.empty() : Mono.just(users.get(0));
                });
    }

}
