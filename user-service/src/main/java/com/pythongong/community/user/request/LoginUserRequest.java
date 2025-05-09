package com.pythongong.community.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginUserRequest(
        @NotEmpty(message = "Username cannot be empty")
        @Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User name can only contain letters, numbers, underscores and hyphens")
        String userName,

        @NotEmpty(message = "Password cannot be empty")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User password can only contain letters, numbers, underscores and hyphens")
        String userPassword) {
}
