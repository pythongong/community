package com.pythongong.community.infras.exception;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import lombok.NonNull;

public class ExceptionUtil {

    public static Throwable extractAsyncExcep(@NonNull Throwable throwable) {
        if ((throwable instanceof CompletionException || throwable instanceof ExecutionException)
                && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }
}
