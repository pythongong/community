package com.pythongong.community.user.service;

import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Value;
import com.pythongong.community.user.repo.CommunityUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GRpcService
@RequiredArgsConstructor
public class CommnityUserService {

    public static final String USER_INSERTION_ERROR = "User insertion failed";

    @Value("${user-service.defalut-avatar}")
    private String defaultAvatar;

    private final CommunityUserRepo communityUserRepo;

    private final TokenService tokenService;

    // public Mono<LoginUserVo> login(LoginUserRequest request, ServerWebExchange
    // exchange) {
    // String userName = request.userName();

    // return communityUserRepo.selectOneByUserName(userName).map(user -> {
    // if (user == null) {
    // throw new CommunityException("User does not exist",
    // CommunityException.COMMON_ERROR);
    // }
    // String password = user.getUserPassword();
    // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    // if (!encoder.matches(request.userPassword(), password)) {
    // throw new CommunityException("Password is wrong",
    // CommunityException.COMMON_ERROR);
    // }
    // AuthUserInfo authUserInfo = new AuthUserInfo(user.getId(),
    // user.getUserType());

    // String accessToken = tokenService.generateAccessToken(authUserInfo);
    // String refreshToken = tokenService.generateRefreshToken(authUserInfo);

    // tokenService.setRefreshTokenCookie(exchange, refreshToken);
    // return new LoginUserVo(userName, user.getUserType(), accessToken);
    // });
    // }

}
