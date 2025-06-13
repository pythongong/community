package com.pythongong.community.infras.web;

import com.google.protobuf.Message;
import com.pythongong.community.infras.proto.IntVal;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;

public class WebUtil {
    private WebUtil() {

    }

    public static final IntVal SUCCESS_RPC = IntVal.newBuilder().setVal(0).build();

    public static final int SUCCESS_CODE = 0;

    public static final String SUCCESS_MSG = "SUCCESS!";

    public static CommunityResponse respondOk(@NonNull Object data) {
        return new CommunityResponse(SUCCESS_CODE, data);
    }

    public static CommunityResponse respondOk() {
        return respondOk(SUCCESS_MSG);
    }

    public static <T extends Message> void respondRpcOK(@NonNull StreamObserver<T> responseObserver, T data) {

        // Success response
        responseObserver.onNext(data);
        responseObserver.onCompleted();

    }

    public static void respondRpcOK(@NonNull StreamObserver<IntVal> responseObserver) {
        respondRpcOK(responseObserver, SUCCESS_RPC);
    }

    public static <T> void respondRpcError(@NonNull StreamObserver<T> responseObserver, @NonNull Throwable error) {
        if (error.getCause() == null) {
            responseObserver.onError(
                    io.grpc.Status.ABORTED.withDescription(error.getMessage()).asRuntimeException());
        }
        responseObserver.onError(
                io.grpc.Status.ABORTED.withDescription(error.getCause().getMessage()).asRuntimeException());

    }

    public static <T> void respondInvalidRpcArgus(@NonNull StreamObserver<T> responseObserver,
            @NonNull String errorMsg) {
        responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(errorMsg).asRuntimeException());
    }

}
