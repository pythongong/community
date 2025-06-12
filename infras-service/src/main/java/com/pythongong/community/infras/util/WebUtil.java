package com.pythongong.community.infras.util;

import com.pythongong.community.infras.proto.IntVal;
import com.pythongong.community.infras.web.CommunityResponse;

import io.micrometer.common.lang.NonNull;

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

}
