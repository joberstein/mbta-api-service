package com.jesseoberstein.controller;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import com.jesseoberstein.model.dto.Listener;
import com.jesseoberstein.service.ConnectionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest(StreamListenersController.class)
public class StreamListenersControllerTest {

    @MockBean
    ConnectionService connectionService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void testGetStreamClients_present() {
        var streamId = "routeId:stopId";

        var listener = Listener.builder()
            .id("clientId")
            .directionId(5)
            .ttl(Duration.ofMinutes(5))
            .build();

        var connections = new ConcurrentHashMap<>(Map.of(
            streamId,
            Connection.builder()
                .routeId("routeId")
                .stopId("stopId")
                .listeners(Map.of("clientId", Client.fromListener(listener)))
                .build()
        ));

        Mockito.when(connectionService.getConnections()).thenReturn(connections);

        webTestClient
            .get()
            .uri("/streams/{streamId}/listeners", streamId)
            .exchange()
            .expectStatus().isOk();

        Mockito.verify(connectionService).getConnections();
    }

    @Test
    public void testGetStreamClients_absent() {
        var connections = new ConcurrentHashMap<String, Connection>(Collections.emptyMap());
        Mockito.when(connectionService.getConnections()).thenReturn(connections);

        webTestClient
            .get()
            .uri("/streams/streamId/listeners")
            .exchange()
            .expectStatus().isNotFound();

        Mockito.verify(connectionService).getConnections();
    }

    @Test
    public void testAddStreamListener() {
        var streamId = "routeId:stopId";

        var connection = Connection.builder()
            .routeId("routeId")
            .stopId("stopId")
            .build();

        var listener = Listener.builder()
            .id("clientId")
            .directionId(5)
            .ttl(Duration.ofMinutes(5))
            .build();

        Mockito.when(connectionService.connect(anyString(), any(Client.class))).thenReturn(connection);

        webTestClient
            .post()
            .uri("/streams/{streamId}/listeners", streamId)
            .bodyValue(listener)
            .exchange()
            .expectStatus().isCreated();

        var client = Client.fromListener(listener);
        Mockito.verify(connectionService).connect("routeId:stopId", client);
    }

    @Test
    public void testRemoveStreamListener() {
        Mockito.doNothing().when(connectionService).disconnect(anyString(), anyString());

        var streamId = "routeId:stopId";
        var clientId = "clientId";

        webTestClient
            .delete()
            .uri("/streams/{streamId}/listeners/{listenerId}", streamId, clientId)
            .exchange()
            .expectStatus().isNoContent();

        Mockito.verify(connectionService).disconnect(streamId, clientId);
    }
}
