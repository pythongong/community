package com.pythongong.community.infras.web;

public class AuthUserContext {
    private static final ThreadLocal<AuthUserInfo> USER_INFO = new ThreadLocal<>();

    public static AuthUserInfo get() {
        return USER_INFO.get();
    }

    public static AuthUserInfo forceGet() {
        return USER_INFO.get();
    }

    public static void set(AuthUserInfo AuthUserInfo) {
        USER_INFO.set(AuthUserInfo);
    }

    public static void clean() {
        if (USER_INFO.get() != null) {
            USER_INFO.remove();
        }
    }
}
