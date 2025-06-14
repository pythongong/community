package com.pythongong.community.infras.etcd;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.options.PutOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Component responsible for registering a gRPC service instance with etcd.
 * It uses etcd leases to ensure ephemeral registration and health checking via
 * TTL.
 * Implements DisposableBean for graceful deregistration on shutdown.
 */
@Component
public class GrpcServiceRegistration implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GrpcServiceRegistration.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String serviceHost = getHostAddress();

    private final Client etcdClient;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${grpc.service.port}")
    private int servicePort; // Port where the gRPC service is listening

    @Value("${etcd.lease-ttl:10}")
    private long leaseTtl; // Time-to-live for the etcd lease, seconds

    private long leaseId;
    private String registrationKey;
    private ScheduledExecutorService leaseRenewScheduler;
    private ScheduledFuture<?> leaseRenewFuture;

    public GrpcServiceRegistration(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    /**
     * Initializes the registration key and lease scheduler.
     * Called after properties are set.
     */
    @PostConstruct
    public void init() {
        // Unique instance ID, combining service name and a UUID for robustness
        String instanceId = serviceHost + ":" + servicePort + "_" + UUID.randomUUID().toString();
        this.registrationKey = String.format("/services/%s/%s", serviceName, instanceId);
        log.info("Service registration key will be: {}", registrationKey);

        this.leaseRenewScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "etcd-lease-renewal-scheduler");
            t.setDaemon(true); // Allow JVM to exit if this is the only remaining thread
            return t;
        });
    }

    /**
     * Event listener for application ready event.
     * This ensures the service registers with etcd once the Spring Boot application
     * is fully started.
     *
     * @param event ApplicationReadyEvent
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Application is ready. Attempting to register service {} on {}:{}", serviceName, serviceHost,
                servicePort);
        registerService();
    }

    /**
     * Registers the gRPC service instance with etcd.
     * This involves granting a lease and putting the service information under a
     * key.
     */
    private void registerService() {
        try {
            // 1. Grant a new lease
            Lease leaseClient = etcdClient.getLeaseClient();
            long grantedLeaseId = leaseClient.grant(leaseTtl).get().getID();
            this.leaseId = grantedLeaseId;
            log.info("Granted etcd lease with ID: {} and TTL: {} seconds", leaseId, leaseTtl);

            // 2. Put the service instance information (host, port) into etcd with the lease
            ServiceInstance currentInstance = new ServiceInstance(serviceHost, servicePort, serviceName);
            String instanceJson = OBJECT_MAPPER.writeValueAsString(currentInstance);

            etcdClient.getKVClient().put(
                    ByteSequence.from(registrationKey, StandardCharsets.UTF_8),
                    ByteSequence.from(instanceJson, StandardCharsets.UTF_8),
                    PutOption.builder().withLeaseId(leaseId).build()).get(); // blocking for simplicity, in
                                                                             // production consider async handling
                                                                             // and retries
            log.info("Service {} registered at key: {} with value: {}", serviceName, registrationKey, instanceJson);

            // 3. Keep the lease alive
            startLeaseRenewal();

        } catch (Exception e) {
            log.error("Failed to register service {} with etcd: {}", serviceName, e.getMessage(), e);
            // In a production environment, consider more sophisticated retry mechanisms or
            // circuit breakers
        }
    }

    /**
     * Starts the scheduled task for renewing the etcd lease.
     * The lease is renewed at approximately 1/3 of its TTL to ensure it doesn't
     * expire.
     */
    private void startLeaseRenewal() {
        long refreshInterval = leaseTtl / 3; // Renew lease every TTL/3 seconds
        if (refreshInterval <= 0)
            refreshInterval = 1; // Minimum 1 second interval

        leaseRenewFuture = leaseRenewScheduler.scheduleAtFixedRate(() -> {
            try {
                etcdClient.getLeaseClient().keepAliveOnce(leaseId).get();
                log.debug("Lease {} renewed for key {}", leaseId, registrationKey);
            } catch (Exception e) {
                log.error("Failed to renew lease {}: {}", leaseId, e.getMessage());
                // If lease renewal consistently fails, it might indicate etcd connectivity
                // issues.
                // Re-registration or application shutdown might be necessary in a real
                // scenario.
            }
        }, refreshInterval, refreshInterval, TimeUnit.SECONDS);
        log.info("Started etcd lease renewal for lease ID {} with interval {} seconds", leaseId, refreshInterval);
    }

    /**
     * Gracefully deregisters the service from etcd when the application is shutting
     * down.
     * This method is called by Spring's DisposableBean lifecycle hook.
     */
    @Override
    public void destroy() {
        log.info("Application is shutting down. Attempting to deregister service {} from etcd.", serviceName);
        // Stop lease renewal first
        if (leaseRenewFuture != null) {
            leaseRenewFuture.cancel(true);
            log.info("Lease renewal scheduled task cancelled.");
        }
        if (leaseRenewScheduler != null) {
            leaseRenewScheduler.shutdownNow();
            try {
                if (!leaseRenewScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Lease renewal scheduler did not terminate cleanly.");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Delete the key from etcd
        if (registrationKey != null && etcdClient != null) {
            try {
                etcdClient.getKVClient().delete(ByteSequence.from(registrationKey, StandardCharsets.UTF_8)).get();
                log.info("Service {} successfully deregistered from etcd by deleting key: {}", serviceName,
                        registrationKey);
            } catch (Exception e) {
                log.error("Failed to deregister service {} from etcd: {}", serviceName, e.getMessage());
            }
        }

        // Revoke the lease (optional, as key deletion also removes associated lease)
        if (leaseId != 0 && etcdClient != null) {
            try {
                etcdClient.getLeaseClient().revoke(leaseId).get();
                log.info("Lease {} successfully revoked.", leaseId);
            } catch (Exception e) {
                log.warn("Failed to revoke lease {}: {}", leaseId, e.getMessage());
            }
        }

        // Close the etcd client connection
        if (etcdClient != null) {
            try {
                etcdClient.close();
                log.info("Etcd client closed.");
            } catch (Exception e) {
                log.error("Error closing etcd client: {}", e.getMessage(), e);
            }
        }
    }

    private static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}