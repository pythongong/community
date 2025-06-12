package com.pythongong.community.infras.util;

public interface CommunityValidator<T> {
    String verify();

    T getSoruce();
}
