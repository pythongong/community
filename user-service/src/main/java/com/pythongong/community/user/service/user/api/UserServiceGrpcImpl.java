package com.pythongong.community.user.service.user.api;

import com.pythongong.community.infras.common.StringUtil;
import com.pythongong.community.infras.converter.ConverterUtil;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.thread.LoomExecutor;
import com.pythongong.community.infras.validator.ValidatorUtil;
import com.pythongong.community.infras.web.WebUtil;
import com.pythongong.community.user.enums.Gender;
import com.pythongong.community.user.enums.UserStatus;
import com.pythongong.community.user.enums.UserType;
import com.pythongong.community.user.model.user.entity.CommunityUser;
import com.pythongong.community.user.model.user.repo.CommunityUserRepo;
import com.pythongong.community.user.proto.RegisterUserRequest;
import com.pythongong.community.user.proto.UserServiceGrpc.UserServiceImplBase;
import com.pythongong.community.user.service.user.validator.RegisterUserRequestValidator;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// Remove Reactor import
// import reactor.core.publisher.Mono;

import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Remove extra semicolon
// ;;

// Assuming reactor-grpc, the generated base class would be like:
// ReactorUserGrpcServiceGrpc.UserGrpcServiceImplBase
// This example implements the method directly for clarity.
// The actual base class to extend depends on your gRPC + Reactor library.

@Slf4j
@GRpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl extends UserServiceImplBase {

  public static final String USER_INSERTION_ERROR = "User creation failed";

  public static final String USER_NAME_EXIST = "User name exists: ";

  @Value("${user-service.defalut-avatar}")
  private String defaultAvatar;

  private final CommunityUserRepo communityUserRepo;

  // Remove ValidParam field
  private final ValidatorUtil validParam;

  // Add BCryptPasswordEncoder field for injection
  private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void register(RegisterUserRequest request, StreamObserver<IntVal> responseObserver) {
    RegisterUserRequestValidator validator = ConverterUtil.convert(RegisterUserRequestValidator.REQUEST_CONVERTER,
        request);
    String errorMsg = validParam.validate(validator); // Assuming validate() method exists on the validator
    if (!StringUtil.isEmpty(errorMsg)) {
      WebUtil.respondInvalidRpcArgus(responseObserver, errorMsg);
      return;
    }

    String userName = request.getUserName();

    LoomExecutor.execute(() -> {
      if (communityUserRepo.countByUserName(userName) > 0) {
        throw new CommunityException(USER_NAME_EXIST + userName);
      }

      if (communityUserRepo.save(createUser(request)) == null) {
        throw new CommunityException(USER_INSERTION_ERROR);
      }
      return WebUtil.SUCCESS_RPC;
    }).whenComplete((response, error) -> {
      if (error != null) {
        WebUtil.respondRpcError(responseObserver, error);
        return;
      }
      WebUtil.respondRpcOK(responseObserver);

    });
  }

  private CommunityUser createUser(RegisterUserRequest request) {
    return new CommunityUser()
        .setUserName(request.getUserName())
        .setUserPassword(passwordEncoder.encode(request.getUserPassword()))
        .setGender(Gender.getValue(request.getGender()))
        .setNickName(request.getNickName())
        .setAvatar(StringUtil.isEmpty(request.getAvatar())
            ? defaultAvatar // Use default if empty
            : request.getAvatar()) // Use provided if not empty
        .setUserProfile(request.getUserProfile())
        .setUserType(UserType.REGULAR.name())
        .setUserStatus(UserStatus.ACTIVE.name())
        .setDeleted(false);
  }

}