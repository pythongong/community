package com.pythongong.community.user.grpc;

import com.pythongong.community.infras.proto.IntVal;
// Remove ValidParam import
// import com.pythongong.community.infras.util.ValidParam;
import com.pythongong.community.infras.util.ConverterUtil;
import com.pythongong.community.infras.util.StringUtil;
import com.pythongong.community.infras.util.ValidParam;
import com.pythongong.community.infras.util.WebUtil;
import com.pythongong.community.user.enums.Gender;
import com.pythongong.community.user.model.CommunityUser;
import com.pythongong.community.user.proto.RegisterUserRequest;
import com.pythongong.community.user.proto.UserServiceGrpc.UserServiceImplBase;
// Update import to the new JPA repo
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.validator.RegisterUserRequestValidator;

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
  private final ValidParam validParam;

  // Add BCryptPasswordEncoder field for injection
  private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void register(RegisterUserRequest request, StreamObserver<IntVal> responseObserver) {
    // Use ConverterUtil and validator directly
    RegisterUserRequestValidator validator = ConverterUtil.convert(RegisterUserRequestValidator.REQUEST_CONVERTER,
        request);
    String errorMsg = validParam.validate(validator); // Assuming validate() method exists on the validator
    if (!StringUtil.isEmpty(errorMsg)) {
      responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(errorMsg).asRuntimeException());
      return;
    }

    String userName = request.getUserName();

    try {
      // Blocking call to JPA repository
      long count = communityUserRepo.countByUserName(userName);

      if (count > 0) {
        responseObserver.onError(io.grpc.Status.ALREADY_EXISTS.withDescription(
            USER_NAME_EXIST + userName).asRuntimeException()); // Added userName to error message
        return;
      }

      CommunityUser communityUser = createUser(request);
      // Blocking call to JPA repository
      CommunityUser savedUser = communityUserRepo.save(communityUser);

      if (savedUser == null || savedUser.getId() == null) {
        responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION.withDescription(
            USER_INSERTION_ERROR).asRuntimeException());
        return;
      }

      // Success response
      responseObserver.onNext(WebUtil.SUCCESS_RPC);
      responseObserver.onCompleted();

    } catch (Exception e) {
      // Handle potential exceptions during database operations
      log.error("Error during user registration for user: {}", userName, e);
      responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Internal server error during registration")
          .withCause(e).asRuntimeException());
    }
  }

  private CommunityUser createUser(RegisterUserRequest request) {
    // Use injected passwordEncoder
    // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); // Remove this
    // line
    return new CommunityUser()
        .setUserName(request.getUserName())
        .setUserPassword(passwordEncoder.encode(request.getUserPassword())) // Use injected encoder
        .setGender(Gender.getValue(request.getGender()))
        .setNickName(request.getNickName())
        .setAvatar(StringUtil.isEmpty(request.getAvatar())
            ? defaultAvatar // Use default if empty
            : request.getAvatar()) // Use provided if not empty
        .setUserProfile(request.getUserProfile());
  }

}