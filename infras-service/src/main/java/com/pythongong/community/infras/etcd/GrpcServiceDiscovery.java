package com.pythongong.community.infras.etcd;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Component for client-side gRPC service discovery using etcd.
 * It dynamically discovers and maintains a list of available service instances
 * by watching changes in etcd. It also provides a basic round-robin load
 * balancing.
 */
@Slf4j
@Component
public class GrpcServiceDiscovery implements DisposableBean {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Client etcdClient;

    // A map to hold the active service instances for each service name
    // Using CopyOnWriteArrayList for thread-safe iteration while allowing
    // modifications
    // Using ConcurrentHashMap for thread-safe access to service lists
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ServiceInstance>> serviceInstancesMap = new ConcurrentHashMap<>();

    // Executor for watch operations to avoid blocking main threads
    private final ExecutorService watchExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "etcd-watch-thread");
        t.setDaemon(true); // Allow JVM to exit if this is the only remaining thread
        return t;
    });

    // Keep track of active watchers to close them on shutdown
    private final Set<Watcher> activeWatchers = ConcurrentHashMap.newKeySet();

    // For round-robin load balancing
    private final ConcurrentHashMap<String, AtomicInteger> serviceIndexMap = new ConcurrentHashMap<>();

    public GrpcServiceDiscovery(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    /**
     * Fetches all current instances for a given service name from etcd and updates
     * the internal cache. Also sets up a watch for future changes if not already
     * active.
     *
     * @param serviceName The logical name of the service (e.g., "userService").
     */
    public synchronized void discoverService(String serviceName) {
        String prefix = "/services/" + serviceName + "/";
        ByteSequence prefixBytes = ByteSequence.from(prefix, StandardCharsets.UTF_8);

        // Initial fetch of all current instances for the service
        try {
            log.info("Fetching initial instances for service: {}", serviceName);
            GetResponse response = etcdClient.getKVClient().get(
                    prefixBytes,
                    GetOption.builder().isPrefix(true).build()).get();

            CopyOnWriteArrayList<ServiceInstance> currentInstances = new CopyOnWriteArrayList<>();
            response.getKvs().forEach(kv -> {
                try {
                    ServiceInstance instance = OBJECT_MAPPER.readValue(kv.getValue().toString(StandardCharsets.UTF_8),
                            ServiceInstance.class);
                    currentInstances.add(instance);
                    log.debug("Discovered initial instance for {}: {}", serviceName, instance);
                } catch (Exception e) {
                    log.warn("Failed to parse service instance JSON from key {}: {}",
                            kv.getKey().toString(StandardCharsets.UTF_8), e.getMessage());
                }
            });
            serviceInstancesMap.put(serviceName, currentInstances);
            serviceIndexMap.putIfAbsent(serviceName, new AtomicInteger(0)); // Initialize index for round-robin
            log.info("Initial discovery for service '{}' found {} instances.", serviceName, currentInstances.size());

            // Set up a watch for future changes if not already watching this prefix
            startWatchingService(serviceName, prefixBytes, response.getHeader().getRevision() + 1);

        } catch (Exception e) {
            log.error("Failed to perform initial discovery for service {}: {}", serviceName, e.getMessage(), e);
        }
    }

    /**
     * Starts a watch on the specified etcd key prefix to monitor changes in service
     * instances.
     * Events (PUT, DELETE) update the internal cache of service instances.
     *
     * @param serviceName   The logical name of the service.
     * @param prefixBytes   The ByteSequence prefix to watch.
     * @param startRevision The revision from which to start watching.
     */
    private void startWatchingService(String serviceName, ByteSequence prefixBytes, long startRevision) {
        // Prevent duplicate watchers for the same service prefix
        // if (activeWatchers.stream()
        // .anyMatch(w -> w.getWatchRequest().getKey().equals(prefixBytes) &&
        // w.getWatchRequest().isPrefix())) {
        // log.debug("Watcher already active for service prefix: {}", serviceName);
        // return;
        // }

        Watcher watcher = etcdClient.getWatchClient().watch(
                prefixBytes,
                WatchOption.builder()
                        .isPrefix(true)
                        .withRevision(startRevision) // Start watching from the next revision after initial fetch
                        .build(),
                (response) -> {
                    for (WatchEvent event : response.getEvents()) {
                        try {
                            ServiceInstance instance = OBJECT_MAPPER.readValue(
                                    event.getKeyValue().getValue().toString(StandardCharsets.UTF_8),
                                    ServiceInstance.class);
                            CopyOnWriteArrayList<ServiceInstance> instances = serviceInstancesMap.get(serviceName);
                            if (instances == null) {
                                instances = new CopyOnWriteArrayList<>();
                                serviceInstancesMap.put(serviceName, instances);
                            }

                            switch (event.getEventType()) {
                                case PUT:
                                    if (!instances.contains(instance)) {
                                        instances.add(instance);
                                        log.info("Service '{}' instance added: {}", serviceName, instance);
                                    }
                                    break;
                                case DELETE:
                                    if (instances.remove(instance)) {
                                        log.info("Service '{}' instance removed: {}", serviceName, instance);
                                    }
                                    break;
                                default:
                                    log.warn("Unhandled etcd watch event type: {}", event.getEventType());
                            }
                        } catch (Exception e) {
                            log.error("Error processing etcd watch event for service {}: {}", serviceName,
                                    e.getMessage());
                        }
                    }
                }, (throwable) -> {
                    log.error("etcd watch for service {} encountered an error: {}", serviceName,
                            throwable.getMessage(), throwable);
                    // In a production system, implement a robust retry strategy for watchers
                    // This might involve re-fetching all services and restarting the watch from the
                    // latest revision.
                    // For simplicity, this example just logs the error.
                }, () -> {
                    log.info("etcd watch for service {} completed.", serviceName);
                    // This typically happens if the etcd server closes the stream,
                    // or if the client explicitly calls close() on the Watcher.
                    // In a production setup, you would likely re-establish the watch.
                });
        activeWatchers.add(watcher);
        log.info("Started etcd watch for service prefix: {} (from revision {})", prefixBytes.toString(), startRevision);
    }

    /**
     * Returns a ManagedChannel to an available instance of the specified gRPC
     * service,
     * using a simple round-robin load balancing strategy.
     * If no instances are available, it returns null.
     *
     * @param serviceName The logical name of the gRPC service.
     * @return A ManagedChannel to a service instance, or null if no instances are
     *         found.
     */
    public ManagedChannel getManagedChannel(String serviceName) {
        // Ensure initial discovery and watch is active for this service
        if (!serviceInstancesMap.containsKey(serviceName) || serviceInstancesMap.get(serviceName).isEmpty()) {
            discoverService(serviceName); // Attempt initial discovery
            // Give it a moment to discover, or rely on future watch events.
            // For a production app, you might block briefly or use a retry pattern here.
        }

        CopyOnWriteArrayList<ServiceInstance> instances = serviceInstancesMap.getOrDefault(serviceName,
                new CopyOnWriteArrayList<>());

        if (instances.isEmpty()) {
            log.warn("No active instances found for service: {}", serviceName);
            return null;
        }

        // Simple round-robin load balancing
        AtomicInteger index = serviceIndexMap.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int currentIdx = index.getAndIncrement();
        ServiceInstance selectedInstance = instances.get(currentIdx % instances.size());

        // Reset index to avoid overflow and keep it within bounds of list size for
        // future access
        if (index.get() >= instances.size() * 2) { // Reset after two full cycles to minimize modulus operations
            index.set(0);
        }

        log.debug("Selected service instance for {}: {}", serviceName, selectedInstance);

        // Build and return a gRPC ManagedChannel
        // In a real application, you might cache ManagedChannels per instance
        // or use a gRPC client library that integrates with NameResolver for this.
        return NettyChannelBuilder.forAddress(selectedInstance.host(), selectedInstance.port())
                .usePlaintext() // Use plaintext for simplicity, use .useTransportSecurity() for production with
                                // TLS
                .build();
    }

    /**
     * Gracefully cleans up resources (watchers and executor) on application
     * shutdown.
     */
    @Override
    public void destroy() {
        log.info("Shutting down gRPC Service Discovery.");

        // Close all active watchers
        for (Watcher watcher : activeWatchers) {
            try {
                watcher.close();
                log.debug("Closed etcd watcher.");
            } catch (Exception e) {
                log.error("Error closing etcd watcher: {}", e.getMessage(), e);
            }
        }
        activeWatchers.clear();

        // Shut down the watch executor
        if (watchExecutor != null) {
            watchExecutor.shutdownNow();
            try {
                if (!watchExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Watch executor did not terminate cleanly.");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Note: The etcdClient is closed by EtcdClientConfig's destroy method if it
        // were a DisposableBean.
        // Since it's a @Bean, Spring manages its lifecycle, typically closing it when
        // the context shuts down.
    }
}