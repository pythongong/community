package com.pythongong.community.infras.util;

public class ConverterUtil {

    private ConverterUtil() {
    }

    public static <S, T> T convert(CommunityConverter<S, T> converter, S source) {
        return converter.convert(source);
    }

}
