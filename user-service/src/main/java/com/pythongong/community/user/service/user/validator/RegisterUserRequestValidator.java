package com.pythongong.community.user.service.user.validator;

import com.pythongong.community.infras.common.StringUtil;
import com.pythongong.community.infras.converter.CommunityConverter;
import com.pythongong.community.infras.validator.CommunityValidator;
import com.pythongong.community.user.enums.Gender;
import com.pythongong.community.user.proto.RegisterUserRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public class RegisterUserRequestValidator implements CommunityValidator<RegisterUserRequestValidator> {

    private static final java.util.regex.Pattern urlPattern = java.util.regex.Pattern.compile("^https?://.*$");

    public static final CommunityConverter<RegisterUserRequest, RegisterUserRequestValidator> REQUEST_CONVERTER = (
            request) -> {
        return RegisterUserRequestValidator.builder()
                .userName(request.getUserName())
                .userPassword(request.getUserPassword())
                .gender(request.getGender())
                .nickName(request.getNickName())
                .avatar(request.getAvatar())
                .userProfile(request.getUserProfile())
                .build();
    };

    @Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User name can only contain letters, numbers, underscores and hyphens")
    private String userName;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User password can only contain letters, numbers, underscores and hyphens")
    private String userPassword;

    @Size(min = 1, max = 20, message = "Nickname must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Nick name can only contain letters, numbers, underscores and hyphens")
    private String nickName;

    @Size(max = 255, message = "Avatar URL cannot exceed 255 characters")
    private String avatar;

    @Size(max = 100, message = "User profile cannot exceed 100 characters")
    private String userProfile;

    @NotEmpty
    private String gender;

    @Override
    public String verify() {
        if (!StringUtil.isEmpty(avatar) && !urlPattern.matcher(avatar).matches()) {
            return "Avatar must be a valid URL starting with http:// or https://";
        }

        if (Gender.getValue(gender) == -1) {
            return "Invalid gender";
        }

        return StringUtil.EMPTY;
    }

    @Override
    public RegisterUserRequestValidator getSoruce() {
        return this;
    }

}