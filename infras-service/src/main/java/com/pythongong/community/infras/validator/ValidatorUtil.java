package com.pythongong.community.infras.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pythongong.community.infras.common.StringUtil;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.NonNull;

@Component
public class ValidatorUtil {

    private static Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        ValidatorUtil.validator = validator;
    }

    public static <T> String validate(@NonNull CommunityValidator<T> communityValidator, @NonNull T source) {
        String errorMsg = validate(source);
        return StringUtil.isEmpty(errorMsg) ? communityValidator.verify(source) : errorMsg;
    }

    public static <T> String validate(@NonNull T source) {
        Set<ConstraintViolation<T>> violations = validator.validate(source);
        return violations.isEmpty() ? StringUtil.EMPTY
                : violations.stream().map(ConstraintViolation::getMessage).findFirst().orElse("Validation error");

    }
}
