package com.pythongong.community.infras.etcd;

public record ServiceInstance(String host, int port, String serviceName) {

}
