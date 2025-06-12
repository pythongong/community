package com.pythongong.community.user.grpc;

import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.util.ValidParam;
import com.pythongong.community.infras.util.WebUtil;
import com.pythongong.community.user.enums.Gender;
import com.pythongong.community.user.model.CommunityUser;
import com.pythongong.community.user.proto.RegisterUserRequest;
import com.pythongong.community.user.repo.CommunityUserRepo;
import com.pythongong.community.user.validator.RegisterUserRequestValidator;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceGrpcImplTest {

    @InjectMocks
    private UserServiceGrpcImpl userServiceGrpc;

    @Mock
    private CommunityUserRepo communityUserRepo;

    @Mock
    private ValidParam validator;

    @Mock
    private StreamObserver<IntVal> responseObserver;

    private final String DEFAULT_AVATAR = "default_avatar_url";

    @BeforeEach
    void setUp() {
        // Set the defaultAvatar value using reflection or a setter if available
        // For simplicity in test, we can set it directly if the field is accessible
        // Or, if using Spring's TestContext, @Value would be handled.
        // Since we are using MockitoExtension, let's set it directly or via reflection.
        // Assuming the field is private final, reflection is needed.
        // A simpler approach for testing is to make the field non-final and add a
        // setter,
        // or pass it via constructor if not using @Value in test.
        // Let's assume for this test we can set it directly for demonstration.
        // In a real Spring Boot test, @Value would be handled.
        try {
            java.lang.reflect.Field field = UserServiceGrpcImpl.class.getDeclaredField("defaultAvatar");
            field.setAccessible(true);
            field.set(userServiceGrpc, DEFAULT_AVATAR);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private RegisterUserRequest createRegisterRequest(String username, String password, String avatar) {
        return RegisterUserRequest.newBuilder()
                .setUserName(username)
                .setUserPassword(password)
                .setGender(Gender.MALE.name()) // Assuming MALE exists and has a value
                .setNickName("Test Nickname")
                .setAvatar(avatar != null ? avatar : "")
                .setUserProfile("Test Profile")
                .build();
    }

    private CommunityUser createCommunityUser(Long id, String username, String encodedPassword, String avatar) {
        CommunityUser user = new CommunityUser();
        user.setId(id);
        user.setUserName(username);
        user.setUserPassword(encodedPassword);
        user.setGender(Gender.MALE.getValue());
        user.setNickName("Test Nickname");
        user.setAvatar(avatar);
        user.setUserProfile("Test Profile");
        return user;
    }

    @Test
    void register_success() {
        // Arrange
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";
        Long userId = 1L;
        RegisterUserRequest request = createRegisterRequest(username, password, "custom_avatar");
        CommunityUser savedUser = createCommunityUser(userId, username, encodedPassword, "custom_avatar");

        when(validator.validate(any(RegisterUserRequestValidator.class))).thenReturn(null);
        when(communityUserRepo.selectCountByUserName(eq(username))).thenReturn(Mono.just(0L));

        when(communityUserRepo.save(any(CommunityUser.class))).thenReturn(Mono.just(savedUser));

        // Act
        userServiceGrpc.register(request, responseObserver);

        // Assert
        verify(validator).validate(any(RegisterUserRequestValidator.class));
        verify(communityUserRepo).selectCountByUserName(eq(username));

        verify(communityUserRepo).save(argThat(user -> user.getUserName().equals(username) &&
                user.getAvatar().equals("custom_avatar") // Verify custom avatar is used
        // Add other field checks as needed
        ));
        verify(responseObserver).onNext(eq(WebUtil.SUCCESS_RPC));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void register_success_withDefaultAvatar() {
        // Arrange
        String username = "testuser2";
        String password = "password2";
        String encodedPassword = "encodedPassword2";
        Long userId = 2L;
        RegisterUserRequest request = createRegisterRequest(username, password, ""); // Empty avatar
        CommunityUser savedUser = createCommunityUser(userId, username, encodedPassword, DEFAULT_AVATAR);

        when(validator.validate(any(RegisterUserRequestValidator.class))).thenReturn(null);
        when(communityUserRepo.selectCountByUserName(eq(username))).thenReturn(Mono.just(0L));

        when(communityUserRepo.save(any(CommunityUser.class))).thenReturn(Mono.just(savedUser));

        // Act
        userServiceGrpc.register(request, responseObserver);

        // Assert
        verify(validator).validate(any(RegisterUserRequestValidator.class));
        verify(communityUserRepo).selectCountByUserName(eq(username));

        verify(communityUserRepo).save(argThat(user -> user.getUserName().equals(username) &&

                user.getAvatar().equals(DEFAULT_AVATAR) // Verify default avatar is used
        // Add other field checks as needed
        ));
        verify(responseObserver).onNext(eq(WebUtil.SUCCESS_RPC));
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void register_userNameExists_returnsAlreadyExistsError() {
        // Arrange
        String username = "existinguser";
        RegisterUserRequest request = createRegisterRequest(username, "password", null);

        when(validator.validate(any(RegisterUserRequestValidator.class))).thenReturn(null);
        when(communityUserRepo.selectCountByUserName(eq(username))).thenReturn(Mono.just(1L)); // User exists

        // Act
        userServiceGrpc.register(request, responseObserver);

        // Assert
        verify(validator).validate(any(RegisterUserRequestValidator.class));
        verify(communityUserRepo).selectCountByUserName(eq(username));
        verify(responseObserver).onError(argThat(error -> error instanceof StatusRuntimeException &&
                ((StatusRuntimeException) error).getStatus().getCode() == Status.ALREADY_EXISTS.getCode()));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        verify(communityUserRepo, never()).save(any());
    }

    @Test
    void register_validationFails_returnsInvalidArgumentError() {
        // Arrange
        String errorMsg = "Invalid username format";
        RegisterUserRequest request = createRegisterRequest("invalid!", "password", null);

        when(validator.validate(any(RegisterUserRequestValidator.class))).thenReturn(errorMsg);

        // Act
        userServiceGrpc.register(request, responseObserver);

        // Assert
        verify(validator).validate(any(RegisterUserRequestValidator.class));
        verify(responseObserver).onError(argThat(error -> error instanceof StatusRuntimeException &&
                ((StatusRuntimeException) error).getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(communityUserRepo, never()).selectCountByUserName(any());

        verify(communityUserRepo, never()).save(any());
    }

    @Test
    void register_saveFails_returnsFailedPreconditionError() {
        // Arrange
        String username = "testuser3";
        String password = "password3";
        RegisterUserRequest request = createRegisterRequest(username, password, null);

        when(validator.validate(any(RegisterUserRequestValidator.class))).thenReturn(null);
        when(communityUserRepo.selectCountByUserName(eq(username))).thenReturn(Mono.just(0L));

        when(communityUserRepo.save(any(CommunityUser.class))).thenReturn(Mono.empty()); // Save
        // returns
        // user
        // without ID

        // Act
        userServiceGrpc.register(request, responseObserver);

        // Assert
        verify(validator).validate(any(RegisterUserRequestValidator.class));
        verify(communityUserRepo).selectCountByUserName(eq(username));

        verify(communityUserRepo).save(any(CommunityUser.class)); // Verify save was called
        verify(responseObserver).onError(argThat(error -> error instanceof StatusRuntimeException &&
                ((StatusRuntimeException) error).getStatus().getCode() == Status.FAILED_PRECONDITION.getCode() &&
                UserServiceGrpcImpl.USER_INSERTION_ERROR.equals(
                        ((StatusRuntimeException) error).getStatus().getDescription())));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

}
