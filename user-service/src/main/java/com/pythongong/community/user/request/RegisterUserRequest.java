package com.pythongong.community.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User name can only contain letters, numbers, underscores and hyphens")
    String userName,

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User password can only contain letters, numbers, underscores and hyphens")
    String userPassword,


        @NotEmpty(message = "Nickname cannot be empty")
        @Size(min = 1, max = 20, message = "Nickname must be between 1 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nick name can only contain letters, numbers, underscores and hyphens")
        String nickName,

        @Size(max = 255, message = "Avatar URL cannot exceed 255 characters")
        @Pattern(regexp = "^https?://.*$", message = "Avatar must be a valid URL starting with http:// or https://")
        String avatar,

        @Size(max = 100, message = "User profile cannot exceed 100 characters")
        String userProfile,

        @NotEmpty String gender) {

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RegisterUserRequest(");
        sb.append("userName=").append(userName);
        sb.append(", userPassword=").append(userPassword);
        sb.append(", gender=").append(gender);
        sb.append(")");
        return sb.toString();
    }
}