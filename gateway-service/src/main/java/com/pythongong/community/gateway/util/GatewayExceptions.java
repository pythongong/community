package com.pythongong.community.gateway.util;

import com.pythongong.community.infras.exception.CommunityException;

public class GatewayExceptions {

    private GatewayExceptions() {
    }

    public static final CommunityException EXPIRED_JWT = new CommunityException("Expired JWT Token", 1001);

}
