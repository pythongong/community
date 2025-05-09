package com.pythongong.community.user.repo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.enums.UserStatus;
import com.pythongong.community.user.repo.CustomCommunityUserRepo;

import reactor.core.publisher.Mono;

@Repository
public class CommunityUserRepoImpl implements CustomCommunityUserRepo {

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Long> selectCountByUserName(String userName) {
        Criteria criteria = Criteria.where(CommunityUser.USER_NAME).is(userName)
        .and(CommunityUser.USER_STATUS).is(UserStatus.ACTIVE.name());
        return entityTemplate.count(Query.query(criteria), CommunityUser.class);
        
    } 

    @Override
    public Mono<CommunityUser> selectOneByUserName(String userName) {
        Criteria criteria = Criteria.where(CommunityUser.USER_NAME).is(userName)
        .and(CommunityUser.USER_STATUS).is(UserStatus.ACTIVE.name());
        return entityTemplate.select(Query.query(criteria), CommunityUser.class)
                .collectList()
                .flatMap(users -> {
                    if (users.size() > 1) {
                        throw new CommunityException("Found more than one user with username: " + userName, CommunityException.COMMON_ERROR);
                    }
                    return users.isEmpty() ? Mono.empty() : Mono.just(users.get(0));
                });
    }

}
