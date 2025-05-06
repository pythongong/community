package com.pythongong.community.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.user.constant.enums.Gender;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.enums.UserType;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.request.RegisterUserRequest;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CommnityUserService {

    public static final String GENDER_INVALID = "";

    private static final UserType DEFAUL_TYPE = UserType.REGULAR;

    @Autowired
    private CommunityUserRepo communityUserRepo;

    public Mono<Void> register(RegisterUserRequest registerUserRequest) {

        int genderVal = Gender.getValue(registerUserRequest.gender());
        if (genderVal == -1) {
            String errorMsg = "Gender is invalid: " + registerUserRequest.gender();
            log.error(errorMsg);
            return Mono.error(
                    new CommunityException(errorMsg, CommunityException.COMMON_ERROR));
        }

        String userName = registerUserRequest.userName();

        return communityUserRepo.selectCountByUserName(userName).flatMap(value -> {
            if (value > 0) {
                return Mono.error(
                        new CommunityException("User name is duplicated", CommunityException.COMMON_ERROR));
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            CommunityUser communityUser = new CommunityUser();
            communityUser.setUserName(userName)
                    .setUserPassword(encoder.encode(registerUserRequest.userPassword()))
                    .setUserType(DEFAUL_TYPE.name())
                    .setGender(genderVal);

            return communityUserRepo.insert(communityUser).flatMap(num -> {
                if (num != 1) {
                    return Mono.error(
                            new CommunityException("User is not inserted", CommunityException.COMMON_ERROR));
                }
                return Mono.empty();
            });
        });

    }

}
