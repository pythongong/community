package com.pythongong.community.infras.web;

// Remove Reactor imports
// import reactor.core.publisher.Mono;
// import reactor.util.context.Context;
// import java.util.function.Function;

public class AuthUserContext {

    // Use a ThreadLocal to store AuthUserInfo for the current thread (virtual or
    // platform)
    private static final ThreadLocal<AuthUserInfo> AUTH_USER_INFO_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Sets the AuthUserInfo for the current thread.
     * This should be called at the beginning of a request or operation scope.
     *
     * @param authUserInfo The AuthUserInfo to set. Can be null to clear.
     */
    public static void set(AuthUserInfo authUserInfo) {
        if (authUserInfo == null) {
            clear(); // Clear if setting null
        } else {
            AUTH_USER_INFO_THREAD_LOCAL.set(authUserInfo);
        }
    }

    /**
     * Retrieves AuthUserInfo from the current thread's ThreadLocal.
     *
     * @return The AuthUserInfo if present, otherwise null.
     */
    public static AuthUserInfo get() {
        return AUTH_USER_INFO_THREAD_LOCAL.get();
    }

    /**
     * Clears the AuthUserInfo from the current thread's ThreadLocal.
     * This is crucial to prevent data leakage between requests when using
     * pooled threads (including virtual threads which can be reused).
     * This should be called at the end of a request or operation scope.
     */
    public static void clear() {
        AUTH_USER_INFO_THREAD_LOCAL.remove();
    }

    // The AUTH_USER_INFO_KEY constant is no longer needed.
    // The get() method no longer returns Mono.
    // The setContext() method is replaced by the imperative set() method.
    // The forceGet() and clear() comments are removed as the new clear() is needed.
}
