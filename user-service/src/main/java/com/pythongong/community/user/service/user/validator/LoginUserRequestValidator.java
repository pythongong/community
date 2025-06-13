package com.pythongong.community.user.service.user.validator;

import com.pythongong.community.infras.converter.CommunityConverter;
import com.pythongong.community.user.proto.LoginUserRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public class LoginUserRequestValidator {

        public static final @NonNull CommunityConverter<LoginUserRequest, LoginUserRequestValidator> REQUEST_CONVERTER = (
                        source) -> {
                return new LoginUserRequestValidator(source.getUserName(), source.getUserPassword());
        };

        @NotBlank(message = "Username cannot be empty")
        @Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User name can only contain letters, numbers, underscores and hyphens")
        String userName;

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User password can only contain letters, numbers, underscores and hyphens")
        String userPassword;

        public LoginUserRequestValidator(String userName, String userPassword) {
                this.userName = userName;
                this.userPassword = userPassword;
        }

}
