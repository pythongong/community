package com.pythongong.community.post.client;

import org.springframework.stereotype.Component;

import com.pythongong.community.grpc.UserServiceGrpc;
import com.pythongong.community.grpc.UserRequest;

import net.devh.boot.grpc.client.inject.GrpcClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public Mono<String> getUserName(Long userId) {
        return Mono.fromCallable(() -> {
            UserRequest request = UserRequest.newBuilder()
                .setUserId(userId)
                .build();
            return userServiceStub.getUserName(request).getUserName();
        });
    }
}