package com.pythongong.community.infras.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidParam {

    private final Validator validator;

    public <T> String validate(@NonNull CommunityValidator<T> communityValidator) {
        Set<ConstraintViolation<T>> violations = validator.validate(communityValidator.getSoruce());
        if (!violations.isEmpty()) {
            return violations.stream().map(ConstraintViolation::getMessage).findFirst().orElse("Validation error");
        }

        return communityValidator.verify();
    }
}
