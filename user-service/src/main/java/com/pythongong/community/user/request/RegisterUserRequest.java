package com.pythongong.community.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotEmpty @Size(min = 1, max = 20) String userName,

        @NotEmpty @Size(min = 8, max = 20) String userPassword,

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