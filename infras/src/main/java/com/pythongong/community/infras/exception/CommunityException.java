package com.pythongong.community.infras.exception;

import lombok.Getter;

@Getter
public class CommunityException extends RuntimeException {

    public static final int COMMON_ERROR = 10;

    private int code;

    public CommunityException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CommunityException(String message) {
        super(message);
    }

}
