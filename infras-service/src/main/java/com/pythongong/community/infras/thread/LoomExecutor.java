package com.pythongong.community.infras.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoomExecutor {

    private static final Executor VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private LoomExecutor() {
    }

    public static <T> CompletableFuture<T> execute(Callable<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                throw new RuntimeException("Error executing operation in virtual thread", e);
            }
        }, VIRTUAL_THREAD_EXECUTOR);
    }

}
