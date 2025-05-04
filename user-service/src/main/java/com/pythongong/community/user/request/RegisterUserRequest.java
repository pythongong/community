package com.pythongong.community.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
                @Size(min = 1, max = 20) @NotEmpty String userName,

                @Size(min = 8, max = 20) @NotEmpty String userPassword) {
}