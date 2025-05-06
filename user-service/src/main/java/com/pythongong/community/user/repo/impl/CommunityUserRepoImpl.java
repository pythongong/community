package com.pythongong.community.user.repo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.pythongong.community.infras.database.ReactiveSql;
import com.pythongong.community.infras.database.SqlColumn;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.repo.CommunityUserRepo;

import reactor.core.publisher.Mono;

@Repository
public class CommunityUserRepoImpl implements CommunityUserRepo {

    @Autowired
    private DatabaseClient databaseClient;

    @Override
    public Mono<Long> selectCountByUserName(String userName) {
        ReactiveSql reactiveSql = new ReactiveSql(databaseClient);
        return reactiveSql.eq(CommunityUser.USER_NAME, userName).count(CommunityUser.TABLE_NAME);
    }

    @Override
    public Mono<Long> insert(CommunityUser communityUser) {
        ReactiveSql reactiveSql = new ReactiveSql(databaseClient);
        List<SqlColumn> dataToBind = List.of(
                new SqlColumn(CommunityUser.USER_NAME, communityUser.getUserName()),
                new SqlColumn(CommunityUser.USER_PASSWORD, communityUser.getUserPassword()),
                new SqlColumn(CommunityUser.USER_TYPE, communityUser.getUserType()),
                new SqlColumn(CommunityUser.GENDER, communityUser.getGender()));
        return reactiveSql.insert(CommunityUser.TABLE_NAME, dataToBind);
    }

}
