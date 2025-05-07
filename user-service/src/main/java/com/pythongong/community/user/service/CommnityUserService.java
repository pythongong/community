package com.pythongong.community.user.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.pythongong.community.infras.config.JwtConfig;
import com.pythongong.community.infras.enums.UserType;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.user.constant.enums.Gender;
import com.pythongong.community.user.domain.CommunityUser;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.request.LoginUserRequest;
import com.pythongong.community.user.request.RegisterUserRequest;
import com.pythongong.community.user.vo.LoginUserVo;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CommnityUserService {

    public static final String USER_INSERTION_ERROR = "User insertion failed";

    public static final String GENDER_INVALID = "";

    private static final UserType DEFAUL_TYPE = UserType.REGULAR;

    @Autowired
    private CommunityUserRepo communityUserRepo;

    @Autowired
    private JwtConfig jwtConfig;

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
                String errorMsg = "User name is duplicated: " + userName;
                log.error(errorMsg);
                return Mono.error(
                        new CommunityException(errorMsg, CommunityException.COMMON_ERROR));
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            CommunityUser communityUser = new CommunityUser();
            communityUser.setUserName(userName)
                    .setUserPassword(encoder.encode(registerUserRequest.userPassword()))
                    .setUserType(DEFAUL_TYPE.name())
                    .setGender(genderVal);

            return communityUserRepo.save(communityUser)
                    .flatMap(user -> {
                        if (user == null) {
                            log.error(USER_INSERTION_ERROR + registerUserRequest.toString());
                            return Mono.error(
                                    new CommunityException(USER_INSERTION_ERROR,
                                            CommunityException.COMMON_ERROR));
                        }
                        return Mono.empty();
                    });
        });
    }

    public Mono<LoginUserVo> login(LoginUserRequest request) {
        String userName = request.userName();

        return communityUserRepo.selectOneByUserName(userName).map(user -> {
            if (user == null) {
                throw new CommunityException("User does not exist", CommunityException.COMMON_ERROR);
            }
            String password = user.getUserPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(request.userPassword(), password)) {
                throw new CommunityException("Password is wrong", CommunityException.COMMON_ERROR);
            }

            String token = Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("userType", user.getUserType())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration() * 1000))
                    .signWith(jwtConfig.secretKey())
                    .compact();
            return new LoginUserVo(userName, user.getUserType(), token);
        });
    }

}
