package com.pythongong.community.infras.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class CommunityException extends RuntimeException {

    public static final int DEFALUT_CODE = 1000;

    private int code;

    public CommunityException(@NonNull String message, int code) {
        super(message);
        this.code = code;
    }

    public CommunityException(@NonNull String message) {
        this(message, DEFALUT_CODE);
    }

}
