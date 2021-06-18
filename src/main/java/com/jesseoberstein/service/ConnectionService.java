package com.jesseoberstein.service;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@AllArgsConstructor
public class ConnectionService {

    @Getter(AccessLevel.PACKAGE)
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();

    private final PredictionStreamingService predictionStreamingService;
    private final NotificationService notificationService;
    private final PredictionProcessor predictionProcessor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final PredictionService predictionService;

    public Connection open(Connection connection, Client client) {
        return connections.compute(connection.getStreamId(), (streamId, existingConnection) -> {
            var openedConnection = Optional.ofNullable(existingConnection)
                .orElseGet(() -> this.initialize(connection));

            this.addListener(openedConnection, client);
            return openedConnection;
        });
    }

    public Connection connect(String connectionId, Client client) {
        return connections.computeIfPresent(connectionId, (id, connection) -> {
            this.addListener(connection, client);
            return connection;
        });
    }

    public void disconnect(String connectionId, String clientId) {
        connections.computeIfPresent(connectionId, (id, connection) -> {
            this.removeListener(connection, clientId);
            return connection;
        });
    }

    public void close(String connectionId) {
        connections.computeIfPresent(connectionId, (id, connection) -> {
            if (connection.getListeners().isEmpty()) {
                log.info("Closing unused connection to stream: {}", connectionId);
                connection.getSubscription().dispose();
                predictionService.delete(connection.getRouteId(), connection.getStopId());

                return null;
            }

            return connection;
        });
    }

    @Async
    @Scheduled(fixedRate = (60 * 1_000))
    public void cleanup() {
        connections.keySet().forEach(this::close);
    }

    @Async
    @Scheduled(fixedRate = (15 * 1_000))
    public void broadcast() {
        connections.keySet().parallelStream().forEach(streamId ->
            connections.computeIfPresent(streamId, (id, connection) -> {
                predictionProcessor.broadcastPredictions(connection.getRouteId(), connection.getStopId());
                return connection;
            })
        );
    }

    private Connection initialize(Connection connection) {
        return connection.toBuilder()
            .subscription(predictionStreamingService.start(connection.getRouteId(), connection.getStopId()))
            .listeners(new ConcurrentHashMap<>())
            .build();
    }

    private void addListener(Connection connection, Client client) {
        connection.getListeners().computeIfAbsent(client.getId(), clientId -> {
            log.info("Adding client '{}' as a listener of stream '{}'", clientId, connection.getStreamId());

            var topicName = buildTopicName(connection, client.getDirectionId());
            notificationService.subscribe(topicName, client.getToken());

            Runnable handleExpiration = () -> this.removeListener(connection, clientId);
            long ttl = client.getTtl().toSeconds();
            var scheduledFuture = scheduledExecutorService.schedule(handleExpiration, ttl, TimeUnit.SECONDS);
            client.setHandleExpirationFuture(scheduledFuture);

            return client;
        });
    }

    private void removeListener(Connection connection, String clientId) {
        connection.getListeners().computeIfPresent(clientId, (id, client) -> {
            log.info("Removing client '{}' as a listener of stream '{}'", clientId, connection.getStreamId());

            var topicName = buildTopicName(connection, client.getDirectionId());
            notificationService.unsubscribe(topicName, client.getToken());
            client.getHandleExpirationFuture().cancel(false);

            return null;
        });
    }

    private String buildTopicName(Connection connection, int directionId) {
        String routeId = connection.getRouteId();
        String stopId = connection.getStopId();

        return String.join(".", routeId, stopId, String.valueOf(directionId));
    }
}
