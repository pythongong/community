package com.pythongong.community.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record LoginUserRequest(
        @NotEmpty @Size(min = 1, max = 20) String userName,

        @NotEmpty @Size(min = 8, max = 20) String userPassword) {

}
