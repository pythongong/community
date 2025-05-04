package com.pythongong.community.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pythongong.community.infras.database.RowRecord;
import com.pythongong.community.infras.enums.Gender;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.domain.metadata.CommunityUserMeta;
import com.pythongong.community.user.enums.UserType;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.request.RegisterUserRequest;

import reactor.core.publisher.Mono;

@Service
public class CommnityUserService {

    private static final UserType DEFAUL_TYPE = UserType.REGULAR;

    @Autowired
    private CommunityUserRepo communityUserRepo;

    public Mono<Void> register(RegisterUserRequest registerUserRequest) {
        String userName = registerUserRequest.userName();

        return communityUserRepo.selectCountByUserName(userName).flatMap(value -> {
            if (value > 0) {
                return Mono.error(
                        new IllegalArgumentException("User name is duplicated"));
            }
           
            return Mono.empty();
        }).flatMap(value -> {
            int genderVal = Gender.getValue(registerUserRequest.gender());
            if (genderVal == -1) {
                return Mono.error(
                        new IllegalArgumentException("Gender is invalid"));
            }

            CommunityUser communityUser = new CommunityUser();
            communityUser.setUserName(userName)
            .setUserPassword(registerUserRequest.userPassword())
            .setUserType(DEFAUL_TYPE.name())
            .setGender(genderVal);
            return communityUserRepo.insert(communityUser);
        });

    }

}
