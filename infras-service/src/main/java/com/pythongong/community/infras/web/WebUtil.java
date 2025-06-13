package com.pythongong.community.infras.web;

import com.google.protobuf.Message;
import com.pythongong.community.infras.common.StringUtil;
import com.pythongong.community.infras.converter.ConverterUtil;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.validator.CommunityValidator;
import com.pythongong.community.infras.validator.ValidatorUtil;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;

public class WebUtil {
    private WebUtil() {

    }

    public static final IntVal SUCCESS_RPC = IntVal.newBuilder().setVal(0).build();

    public static final int SUCCESS_CODE = 0;

    public static final String SUCCESS_MSG = "SUCCESS!";

    public static <T> void respondRpcError(@NonNull StreamObserver<T> responseObserver, @NonNull Throwable error) {
        if (error.getCause() == null) {
            responseObserver.onError(
                    io.grpc.Status.ABORTED.withDescription(error.getMessage()).asRuntimeException());
        }
        responseObserver.onError(
                io.grpc.Status.ABORTED.withDescription(error.getCause().getMessage()).asRuntimeException());

    }

    public static <T extends Message> void respondRpc(@NonNull StreamObserver<T> responseObserver, T response,
            Throwable error) {
        if (error != null) {
            respondRpcError(responseObserver, error);
            return;
        }

        if (response == null) {
            respondRpcError(responseObserver, new CommunityException("RPC data is null"));
            return;
        }

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    public static <S, T> boolean validateRpcRequest(@NonNull RpcValidationParam<S, T> param) {
        T target = ConverterUtil.convert(param.converter(), param.source());
        CommunityValidator<T> validator = param.validator();
        String errorMsg = validator == null ? ValidatorUtil.validate(target)
                : ValidatorUtil.validate(validator, target);
        if (StringUtil.isEmpty(errorMsg)) {
            return true;
        }
        param.responseObserver().onError(io.grpc.Status.INVALID_ARGUMENT
                .withDescription(errorMsg)
                .asRuntimeException());
        return false;
    }

}
