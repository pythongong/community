package com.pythongong.community.infras.exception;

import lombok.NonNull;

public record ExcepInfo(@NonNull String message, int code) {

    public static final int DEFALUT_CODE = 1000;

}
