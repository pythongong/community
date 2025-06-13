package com.pythongong.community.infras.validator;

public interface CommunityValidator<T> {
    String verify();

    T getSoruce();
}
