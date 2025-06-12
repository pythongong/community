package com.pythongong.community.infras.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class CommunityException extends RuntimeException {

    private int code;

    public CommunityException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CommunityException(ExcepInfo exceptInfo) {
        this(exceptInfo.message(), exceptInfo.code());
    }

    public CommunityException(@NonNull String message) {
        this(message, ExcepInfo.DEFALUT_CODE);
    }

}
