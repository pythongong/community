package com.pythongong.community.user.grpc;

import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.util.ValidParam;
import com.pythongong.community.infras.util.ConverterUtil;
import com.pythongong.community.infras.util.StringUtil;
import com.pythongong.community.infras.util.WebUtil;
import com.pythongong.community.user.enums.Gender;
import com.pythongong.community.user.model.CommunityUser;
import com.pythongong.community.user.proto.RegisterUserRequest;
import com.pythongong.community.user.proto.UserServiceGrpc.UserServiceImplBase;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.validator.RegisterUserRequestValidator;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.lognet.springboot.grpc.GRpcService;
// For other starters like grpc-spring-boot-starter, the annotation might be different or just @Component
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;;

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

  private final ValidParam validParam;

  @Override
  public void register(RegisterUserRequest request, StreamObserver<IntVal> responseObserver) {
    RegisterUserRequestValidator validator = ConverterUtil.convert(RegisterUserRequestValidator.REQUEST_CONVERTER,
        request);
    String errorMsg = validParam.validate(validator);
    if (!StringUtil.isEmpty(errorMsg)) {
      responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(errorMsg).asRuntimeException());
      return;
    }

    String userName = request.getUserName();
    communityUserRepo.selectCountByUserName(userName)
        .flatMap(count -> {
          if (count > 0) {
            return Mono.error(io.grpc.Status.ALREADY_EXISTS.withDescription(
                USER_NAME_EXIST).asRuntimeException());
          }

          CommunityUser communityUser = createUser(request);
          return communityUserRepo.save(communityUser);

        })
        .switchIfEmpty(Mono.error(io.grpc.Status.FAILED_PRECONDITION.withDescription(
            USER_INSERTION_ERROR).asRuntimeException()))
        .flatMap(savedUser -> {
          return Mono.just(WebUtil.respondOk());
        })
        .subscribe(response -> {
          responseObserver.onNext(WebUtil.SUCCESS_RPC);
          responseObserver.onCompleted();
        }, error -> {
          responseObserver.onError(error);
        });
  }

  private CommunityUser createUser(RegisterUserRequest request) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return new CommunityUser()
        .setUserName(request.getUserName())
        .setUserPassword(encoder.encode(request.getUserPassword()))
        .setGender(Gender.getValue(request.getGender()))
        .setNickName(request.getNickName())
        .setAvatar(StringUtil.isEmpty(request.getAvatar())
            ? defaultAvatar // Use default if empty
            : request.getAvatar()) // Use provided if not empty
        .setUserProfile(request.getUserProfile());
  }

}