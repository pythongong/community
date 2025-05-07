package com.pythongong.community.infras.exception;

import lombok.Getter;

@Getter
public class CommunityException extends RuntimeException {

    public static final int COMMON_ERROR = 100;

    public static final int UNAUTHORIZED = 101;

    private int code;

    public CommunityException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CommunityException(String message) {
        super(message);
    }

}
