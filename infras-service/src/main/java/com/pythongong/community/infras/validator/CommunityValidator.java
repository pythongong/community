package com.pythongong.community.infras.validator;

@FunctionalInterface
public interface CommunityValidator<T> {
    String verify(T source);
}
