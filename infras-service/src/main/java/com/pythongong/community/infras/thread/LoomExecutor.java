package com.pythongong.community.infras.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pythongong.community.infras.exception.CommunityException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoomExecutor {

    public static final ExecutorService IO_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public static final ExecutorService CPU_EXECUTOR = Executors.newWorkStealingPool();

    private LoomExecutor() {
    }

    public static <T> CompletableFuture<T> execute(@NonNull Callable<T> operation,
            @NonNull ExecutorService executorService) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Operation is interrupted: ", e);
                throw new CompletionException("Operation interrupted", e);
            } catch (RuntimeException e) {
                logRuntimExcep(e);
                throw e;
            } catch (Exception e) {
                log.error("Operation failed: ", e);
                throw new CommunityException(e.getMessage());
            }
        }, executorService);
    }

    public static <T> CompletableFuture<T> execute(@NonNull Callable<T> operation) {
        return execute(operation, IO_EXECUTOR);
    }

    public static CompletableFuture<Void> execute(@NonNull Runnable operation,
            @NonNull ExecutorService executorService) {
        return CompletableFuture.runAsync(() -> {
            try {
                operation.run();
            } catch (RuntimeException e) {
                logRuntimExcep(e);
                throw e;
            }
        }, executorService);
    }

    public static CompletableFuture<Void> execute(@NonNull Runnable operation) {
        return execute(operation, IO_EXECUTOR);
    }

    private static void logRuntimExcep(RuntimeException e) {
        if (e instanceof CommunityException) {
            log.warn("Business error: {}", e.getMessage()); // No stack trace
        } else {
            log.error("Unexpected error n operation: ", e); // Full stack trace
        }
    }

    public static void shutdown() {
        IO_EXECUTOR.shutdown();
        CPU_EXECUTOR.shutdown();
    }
}
