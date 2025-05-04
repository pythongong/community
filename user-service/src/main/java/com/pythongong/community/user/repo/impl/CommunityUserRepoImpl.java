package com.pythongong.community.user.repo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.pythongong.community.infras.database.ReactiveSql;
import com.pythongong.community.infras.database.RowRecord;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.domain.metadata.CommunityUserMeta;
import com.pythongong.community.user.repo.CommunityUserRepo;

import reactor.core.publisher.Mono;

@Repository
public class CommunityUserRepoImpl implements CommunityUserRepo {

    @Autowired
    private DatabaseClient databaseClient;

    @Override
    public Mono<Integer> selectCountByUserName(String userName) {
        ReactiveSql reactiveSql = new ReactiveSql(databaseClient);
        return reactiveSql.from(CommunityUserMeta.COMMUNITY_USER).eq(CommunityUserMeta.USER_NAME, userName).count();
    }

    @Override
    public Mono<? extends Void> insert(CommunityUser communityUser) {
        ReactiveSql reactiveSql = new ReactiveSql(databaseClient);
        List<RowRecord> rowRecords = List.of(
                    new RowRecord(CommunityUserMeta.USER_NAME, communityUser.getUserName()),
                    new RowRecord(CommunityUserMeta.USER_PASSWORD, communityUser.getUserPassword()),
                    new RowRecord(CommunityUserMeta.USER_TYPE, communityUser.getUserType()), 
                    new RowRecord(CommunityUserMeta.GENDER, communityUser.getGender().toString()));
        return reactiveSql.insert(CommunityUserMeta.COMMUNITY_USER, rowRecords);
    }

}
