package com.pythongong.community.infras.etcd;

import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EtcdConfig {
    @Value("${etcd.endpoints}")
    private String etcdEndpoints; // Comma-separated list of etcd endpoints, e.g., "http://localhost:2379"

    @Value("${etcd.connection-timeout-millis:5000}")
    private long connectionTimeoutMillis;

    /**
     * Creates and configures the etcd Client bean.
     * This client will be used for all interactions with the etcd cluster.
     *
     * @return A configured jetcd.Client instance.
     * @throws Exception if client creation fails.
     */
    @Bean
    public Client etcdClient() throws Exception {
        List<String> endpointsList = Arrays.stream(etcdEndpoints.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if (endpointsList.isEmpty()) {
            throw new IllegalArgumentException("etcd.endpoints must be configured in application.yml");
        }

        try {
            Client client = Client.builder()
                    .endpoints(endpointsList.toArray(new String[0]))
                    .connectTimeout(Duration.ofMillis(connectionTimeoutMillis))
                    // Add more production-ready configurations here:
                    .maxInboundMessageSize(16 * 1024 * 1024) // Example: Increase gRPC message size limit
                    .build();

            // Simple connectivity check (optional, but good for early detection)
            // client.getKVClient().get(ByteSequence.from("health_check")).get(5,
            // TimeUnit.SECONDS);
            log.info("Initializing etcd client with endpoints: {}", etcdEndpoints);
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize etcd client with endpoints {}: {}", etcdEndpoints, e.getMessage(), e);
            throw e; // Re-throw to prevent application startup if etcd connection is critical
        }
    }

}
