package com.pythongong.community.user.service.user;

import com.pythongong.community.infras.common.StringUtil;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.proto.LongVal;
import com.pythongong.community.infras.thread.LoomExecutor;
import com.pythongong.community.infras.web.RpcValidationParam;
import com.pythongong.community.infras.web.WebUtil;
import com.pythongong.community.user.model.user.entity.CommunityUser;
import com.pythongong.community.user.model.user.repo.CommunityUserRepo;
import com.pythongong.community.user.proto.LoginUserRequest;
import com.pythongong.community.user.proto.RegisterUserRequest;
import com.pythongong.community.user.proto.UserServiceGrpc.UserServiceImplBase;
import com.pythongong.community.user.service.user.enums.Gender;
import com.pythongong.community.user.service.user.enums.UserStatus;
import com.pythongong.community.user.service.user.enums.UserType;
import com.pythongong.community.user.service.user.validator.LoginUserRequestValidator;
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

  // Add BCryptPasswordEncoder field for injection
  private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void register(RegisterUserRequest request, StreamObserver<IntVal> responseObserver) {
    if (!WebUtil.validateRpcRequest(
        RpcValidationParam.<RegisterUserRequest, RegisterUserRequestValidator>builder()
            .responseObserver(responseObserver)
            .source(request)
            .converter(RegisterUserRequestValidator.REQUEST_CONVERTER)
            .validator(RegisterUserRequestValidator.REQUEST_VALIDATOR)
            .build()

    )) {
      return;
    }
    String userName = request.getUserName();

    LoomExecutor.execute(() -> {
      if (communityUserRepo.countByUserName(userName) > 0) {
        throw new CommunityException(USER_NAME_EXIST + userName);
      }
    }).thenAccept((res) -> {
      if (communityUserRepo.save(createUser(request)) == null) {
        throw new CommunityException(USER_INSERTION_ERROR);
      }
    }).whenComplete((response, error) -> {
      WebUtil.respondRpc(responseObserver, error);
    });
  }

  @Override
  public void login(LoginUserRequest request, StreamObserver<LongVal> responseObserver) {

    if (!WebUtil.validateRpcRequest(
        RpcValidationParam.<LoginUserRequest, LoginUserRequestValidator>builder()
            .responseObserver(responseObserver)
            .source(request)
            .converter(LoginUserRequestValidator.REQUEST_CONVERTER)
            .build())) {
      return;
    }

    LoomExecutor.execute(() -> {
      CommunityUser user = communityUserRepo.findByUserName(request.getUserName())
          .orElseThrow(() -> new CommunityException("User does not register"));
      String password = user.getUserPassword();
      if (!passwordEncoder.matches(request.getUserPassword(), password)) {
        throw new CommunityException("Wrong password!");
      }
      return LongVal.newBuilder().setVal(user.getId()).build();
    }).whenComplete((response, error) -> {
      WebUtil.respondRpc(responseObserver, response, error);
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