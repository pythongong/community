package com.pythongong.community.infras.web;

import com.google.protobuf.Message;
import com.pythongong.community.infras.common.StringUtil;
import com.pythongong.community.infras.converter.ConverterUtil;
import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.exception.ExceptionUtil;
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

    private static <T> void respondRpcError(@NonNull StreamObserver<T> responseObserver, @NonNull Throwable error) {
        Throwable asyncExcep = ExceptionUtil.extractAsyncExcep(error);
        if (asyncExcep instanceof CommunityException) {
            responseObserver.onError(
                    io.grpc.Status.ABORTED.withDescription(asyncExcep.getMessage())
                            .asRuntimeException());
        } else {
            responseObserver.onError(
                    io.grpc.Status.INTERNAL.withDescription(asyncExcep.getMessage())
                            .asRuntimeException());
        }

    }

    public static <T extends Message> void respondRpc(@NonNull StreamObserver<T> responseObserver, T response,
            Throwable error) {
        if (error != null) {
            respondRpcError(responseObserver, error);
            return;
        }

        if (response == null) {
            respondRpcError(responseObserver, new CommunityException("RPC response is null"));
            return;
        }

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    public static void respondRpc(@NonNull StreamObserver<IntVal> responseObserver, Throwable error) {
        respondRpc(responseObserver, SUCCESS_RPC, error);
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
