package com.jesseoberstein.service;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConnectionServiceTest {

    @Mock
    private PredictionStreamingService predictionStreamingService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PredictionProcessor predictionProcessor;

    @Mock
    private ScheduledExecutorService executorService;

    @Mock
    private PredictionService predictionService;

    @InjectMocks
    private ConnectionService service;

    @Mock
    private Disposable subscription;

    @Mock
    private ScheduledFuture<?> handleExpirationFuture;

    private Connection connection;
    private Client client;

    @BeforeEach
    public void setup() {
        connection = Connection.builder()
            .routeId("Red")
            .stopId("place-davis")
            .build();

        client = Client.builder()
            .id("A")
            .directionId(1)
            .ttl(Duration.ofMinutes(15))
            .token("token")
            .build();

        Mockito.lenient()
            .when(predictionStreamingService.start(anyString(), anyString()))
            .thenReturn(subscription);

        Mockito.lenient()
            .doNothing()
            .when(notificationService).subscribe(anyString(), anyString());

        Mockito.lenient()
            .doNothing()
            .when(notificationService).unsubscribe(anyString(), anyString());

        Mockito.lenient()
            .doNothing()
            .when(predictionProcessor).broadcastPredictions(anyString(), anyString());

        Mockito.lenient()
            .doNothing()
            .when(predictionService).delete(anyString(), anyString());

        Mockito.lenient()
            .doReturn(handleExpirationFuture)
            .when(executorService).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        Mockito.lenient()
            .when(handleExpirationFuture.cancel(anyBoolean()))
            .thenReturn(true);
    }

    @Test
    public void testOpen_noConnections() {
        service.open(connection.getRouteId(), connection.getStopId(), client);

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());

        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verify(notificationService).subscribe("Red.place-davis.1", client.getToken());
        Mockito.verify(executorService).schedule(any(Runnable.class), eq(client.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testOpen_connectionMissing() {
        var connection2 = connection.toBuilder()
            .routeId("Orange")
            .stopId("place-forhl")
            .build();

        Stream.of(connection, connection2)
            .parallel()
            .forEach(conn -> service.open(conn.getRouteId(), conn.getStopId(), client));

        var expectedConnections = Map.ofEntries(
            Map.entry(
                connection.getStreamId(),
                buildExpectedConnection(connection, client)),
            Map.entry(
                connection2.getStreamId(),
                buildExpectedConnection(connection2, client))
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());

        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verify(predictionStreamingService).start(connection2.getRouteId(), connection2.getStopId());
        Mockito.verify(notificationService).subscribe("Orange.place-forhl.1", client.getToken());
        Mockito.verify(notificationService).subscribe("Red.place-davis.1", client.getToken());
        Mockito.verify(executorService, times(2))
            .schedule(any(Runnable.class), eq(client.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testOpen_connectionExists() {
        var client2 = client.toBuilder()
            .id("B")
            .ttl(Duration.ofMinutes(30))
            .directionId(0)
            .token("token2")
            .build();

        Stream.of(client, client2)
            .parallel()
            .forEach(cli -> service.open(connection.getRouteId(), connection.getStopId(), cli));

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client, client2)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());

        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verify(notificationService).subscribe("Red.place-davis.1", client.getToken());
        Mockito.verify(notificationService).subscribe("Red.place-davis.0", client2.getToken());
        Mockito.verify(executorService).schedule(any(Runnable.class), eq(client.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
        Mockito.verify(executorService).schedule(any(Runnable.class), eq(client2.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
    }


    @Test
    public void testOpen_connectionAndClientExist() {
        service.open(connection.getRouteId(), connection.getStopId(), client);
        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verifyNoMoreInteractions(predictionStreamingService);

        var openedConnection = service.open(connection.getRouteId(), connection.getStopId(), client);
        var expectedConnection = buildExpectedConnection(connection, client);
        Assertions.assertEquals(expectedConnection, openedConnection);

        var expectedConnections = Map.of(expectedConnection.getStreamId(), expectedConnection);
        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());
    }

    @Test
    public void testConnect_connectionMissing() {
        var connected = service.connect("test", client);

        Assertions.assertNull(connected);
        Assertions.assertEquals(new ConcurrentHashMap<>(), service.getConnections());

        Mockito.verifyNoInteractions(predictionStreamingService);
        Mockito.verifyNoInteractions(notificationService);
        Mockito.verifyNoInteractions(executorService);
    }

    @Test
    public void testConnect_connectionExists() {
        var client2 = client.toBuilder()
            .id("B")
            .directionId(0)
            .ttl(Duration.ofMinutes(30))
            .token("token2")
            .build();

        service.open(connection.getRouteId(), connection.getStopId(), client);
        service.connect(connection.getStreamId(), client2);

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client, client2)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());

        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verify(notificationService).subscribe("Red.place-davis.1", client.getToken());
        Mockito.verify(notificationService).subscribe("Red.place-davis.0", client2.getToken());
        Mockito.verify(executorService).schedule(any(Runnable.class), eq(client.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
        Mockito.verify(executorService).schedule(any(Runnable.class), eq(client2.getTtl().toSeconds()), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testConnect_connectionAndClientExist() {
        service.open(connection.getRouteId(), connection.getStopId(), client);
        Mockito.verify(predictionStreamingService).start(connection.getRouteId(), connection.getStopId());
        Mockito.verifyNoMoreInteractions(predictionStreamingService);

        var connected = service.connect(connection.getStreamId(), client);
        var expectedConnection = buildExpectedConnection(connection, client);
        Assertions.assertEquals(expectedConnection, connected);

        var expectedConnections = Map.of(expectedConnection.getStreamId(), expectedConnection);
        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());
    }

    @Test
    public void testDisconnect_connectionMissing() {
        var disconnected = service.connect("test", client);

        Assertions.assertNull(disconnected);
        Assertions.assertEquals(new ConcurrentHashMap<>(), service.getConnections());
        Mockito.verifyNoInteractions(notificationService);
        Mockito.verifyNoInteractions(handleExpirationFuture);
    }

    @Test
    public void testDisconnect_clientMissing() {
        service.open(connection.getRouteId(), connection.getStopId(), client);
        Mockito.verify(notificationService).subscribe("Red.place-davis.1", client.getToken());
        Mockito.verifyNoMoreInteractions(notificationService);

        service.disconnect(connection.getStreamId(), "test");

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());
        Mockito.verifyNoInteractions(handleExpirationFuture);
    }

    @Test
    public void testDisconnect_connectionAndClientExist() {
        var client2 = client.toBuilder()
            .id("B")
            .build();

        Stream.of(client, client2)
            .parallel()
            .forEach(cli -> service.open(connection.getRouteId(), connection.getStopId(), cli));

        service.disconnect(connection.getStreamId(), client.getId());

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client2)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());
        Mockito.verify(notificationService).unsubscribe("Red.place-davis.1", client.getToken());
        Mockito.verify(handleExpirationFuture).cancel(false);
    }

    @Test
    public void testClose_connectionMissing() {
        service.close("test");
        Assertions.assertEquals(new ConcurrentHashMap<>(), service.getConnections());
        verifyNoInteractions(predictionService);
    }

    @Test
    public void testClose_connectionHasListeners() {
        service.open(connection.getRouteId(), connection.getStopId(), client);
        service.close(connection.getStreamId());

        var expectedConnections = Map.of(
            connection.getStreamId(),
            buildExpectedConnection(connection, client)
        );

        Assertions.assertEquals(new ConcurrentHashMap<>(expectedConnections), service.getConnections());
        verifyNoInteractions(predictionService);
    }

    @Test
    public void testClose_connectionWithoutListeners() {
        service.open(connection.getRouteId(), connection.getStopId(), client);
        service.disconnect(connection.getStreamId(), client.getId());
        service.close(connection.getStreamId());

        Assertions.assertEquals(new ConcurrentHashMap<>(), service.getConnections());
        verify(predictionService).delete(connection.getRouteId(), connection.getStopId());
    }

    private Connection buildExpectedConnection(Connection connection, Client... clients) {
        var listeners = Arrays.stream(clients)
            .collect(Collectors.toMap(Client::getId, client -> client));

        return connection.toBuilder()
            .subscription(subscription)
            .listeners(listeners)
            .build();
    }
}
