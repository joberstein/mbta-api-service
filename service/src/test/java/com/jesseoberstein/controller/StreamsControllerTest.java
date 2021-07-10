package com.jesseoberstein.controller;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import com.jesseoberstein.model.dto.Listener;
import com.jesseoberstein.model.dto.Stream;
import com.jesseoberstein.service.ConnectionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebFluxTest(StreamsController.class)
public class StreamsControllerTest {

    @MockBean
    ConnectionService connectionService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void testGetStreams() {
        var connections = new ConcurrentHashMap<>(Map.of(
            "routeId:stopId",
            Connection.builder()
                .routeId("routeId")
                .stopId("stopId")
                .listeners(new ConcurrentHashMap<>())
                .build()
        ));

        Mockito.when(connectionService.getConnections()).thenReturn(connections);

        var streams = List.of(
            Stream.builder()
                .id("routeId:stopId")
                .routeId("routeId")
                .stopId("stopId")
                .build()
        );

        webTestClient
            .get()
            .uri("/streams")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Stream.class).isEqualTo(streams);

        Mockito.verify(connectionService).getConnections();
    }

    @Test
    public void testCreateStream() {
        var listener = Listener.builder()
            .id("clientId")
            .directionId(5)
            .ttl(Duration.ofMinutes(5))
            .build();

        var client = Client.fromListener(listener);

        var routeId = "routeId";
        var stopId = "stopId";
        var connection = Connection.builder()
            .routeId(routeId)
            .stopId(stopId)
            .build();

        Mockito.when(connectionService.open(routeId, stopId, client)).thenReturn(connection);

        webTestClient
            .post()
            .uri("/streams?routeId={routeId}&stopId={stopId}", routeId, stopId)
            .bodyValue(listener)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(String.class).isEqualTo(routeId + ":" + stopId);

        Mockito.verify(connectionService).open(routeId, stopId, client);
    }

    @Test
    public void testGetStream_present() {
        var streamId = "routeId:stopId";
        var connections = new ConcurrentHashMap<>(Map.of(
            streamId,
            Connection.builder()
                .routeId("routeId")
                .stopId("stopId")
                .listeners(new ConcurrentHashMap<>())
                .build()
        ));

        Mockito.when(connectionService.getConnections()).thenReturn(connections);

        var stream = Stream.builder()
            .id(streamId)
            .routeId("routeId")
            .stopId("stopId")
            .build();

        webTestClient
            .get()
            .uri("/streams/{streamId}", streamId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Stream.class).isEqualTo(stream);

        Mockito.verify(connectionService).getConnections();
    }

    @Test
    public void testGetStream_absent() {
        var connections = new ConcurrentHashMap<String, Connection>();
        Mockito.when(connectionService.getConnections()).thenReturn(connections);

        webTestClient
            .get()
            .uri("/streams/{streamId}", "routeId:stopId")
            .exchange()
            .expectStatus().isNotFound();

        Mockito.verify(connectionService).getConnections();
    }

    @Test
    public void testStopStream() {
        var streamId = "routeId:stopId";
        Mockito.doNothing().when(connectionService).close(streamId);

        webTestClient
            .delete()
            .uri("/streams/{streamId}", streamId)
            .exchange()
            .expectStatus().isNoContent();

        Mockito.verify(connectionService).close(streamId);
    }
}
