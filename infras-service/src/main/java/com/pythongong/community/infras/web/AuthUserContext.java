package com.pythongong.community.infras.web;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public class AuthUserContext {
    // Use the class type itself as a key for type safety and simplicity
    public static final Class<AuthUserInfo> AUTH_USER_INFO_KEY = AuthUserInfo.class;

    /**
     * Retrieves AuthUserInfo from the current Reactor Context.
     * Returns Mono.empty() if no AuthUserInfo is found.
     *
     * @return A Mono emitting AuthUserInfo if present, otherwise an empty Mono.
     */
    public static Mono<AuthUserInfo> get() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(AUTH_USER_INFO_KEY)) {
                return Mono.just(contextView.get(AUTH_USER_INFO_KEY));
            }
            return Mono.empty();
        });
    }

    /**
     * Returns a function that can be used with Mono.contextWrite()
     * to add the provided AuthUserInfo to the Reactor Context.
     *
     * Example usage in a filter:
     * return chain.filter(exchange)
     * .contextWrite(AuthUserContext.setContext(authUserInfo));
     *
     * @param authUserInfo The AuthUserInfo to set in the context.
     * @return A function to modify the context.
     */
    public static Function<Context, Context> setContext(AuthUserInfo authUserInfo) {
        return context -> context.put(AUTH_USER_INFO_KEY, authUserInfo);
    }

    // The forceGet() method is removed as its behavior can be achieved by composing
    // get(), e.g.:
    // AuthUserContext.get().switchIfEmpty(Mono.error(new
    // IllegalStateException("AuthUserInfo not found")));

    // The clear() method is removed as Reactor Context is immutable and scoped to a
    // subscription.
    // There's no need for an explicit clear operation like with ThreadLocal.
}
