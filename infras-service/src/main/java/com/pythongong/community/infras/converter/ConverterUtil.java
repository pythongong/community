package com.pythongong.community.infras.converter;

import lombok.NonNull;

public class ConverterUtil {

    private ConverterUtil() {
    }

    public static <S, T> T convert(@NonNull CommunityConverter<S, T> converter, @NonNull S source) {
        return converter.convert(source);
    }

}
