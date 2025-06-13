package com.pythongong.community.infras.web;

import com.pythongong.community.infras.converter.CommunityConverter;
import com.pythongong.community.infras.validator.CommunityValidator;

import io.grpc.stub.StreamObserver;
import lombok.NonNull;

public record RpcValidationParam<S, T>(@NonNull StreamObserver<?> responseObserver,
        @NonNull S source, @NonNull CommunityConverter<S, T> converter,
        CommunityValidator<T> validator) {

    // Private constructor for the builder to use

    // Static method to get a new builder instance
    public static <S, T> Builder<S, T> builder() {
        return new Builder<>();
    }

    // Static nested Builder class
    public static class Builder<S, T> {
        private StreamObserver<?> responseObserver;
        private S source;
        private CommunityConverter<S, T> converter;
        private CommunityValidator<T> validator;

        // Private constructor to prevent direct instantiation
        private Builder() {
        }

        public Builder<S, T> responseObserver(@NonNull StreamObserver<?> responseObserver) {
            this.responseObserver = responseObserver;
            return this;
        }

        public Builder<S, T> source(@NonNull S source) {
            this.source = source;
            return this;
        }

        public Builder<S, T> converter(@NonNull CommunityConverter<S, T> converter) {
            this.converter = converter;
            return this;
        }

        public Builder<S, T> validator(CommunityValidator<T> validator) {
            this.validator = validator;
            return this;
        }

        public RpcValidationParam<S, T> build() {
            // You might add validation here to ensure required fields are set
            // if (this.responseObserver == null || this.source == null || this.target ==
            // null || this.converter == null) {
            // throw new IllegalStateException("Required fields are not set for
            // RpcValidationParam");
            // }
            return new RpcValidationParam<>(this.responseObserver, this.source, this.converter,
                    this.validator);
        }
    }
}