package com.jesseoberstein.controller;

import com.jesseoberstein.model.mbta.JsonApiResponse;
import com.jesseoberstein.model.mbta.Route;
import com.jesseoberstein.model.mbta.Stop;
import com.jesseoberstein.service.FetchResourceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(MbtaApiController.class)
public class MbtaApiControllerTest {

    @MockBean
    FetchResourceService fetchResourceService;

    @Autowired
    WebTestClient webTestClient;

    private static final List<Route> ROUTES = List.of(
        Route.builder()
            .id("Orange")
            .directionDestinations(new String[]{"Oak Grove", "Forest Hills"})
            .longName("Orange Line")
            .build(),
        Route.builder()
            .id("Red")
            .directionDestinations(new String[]{"Alewife", "Ashmont/Braintree"})
            .longName("Red Line")
            .build()
    );

    private static final List<Stop> STOPS = List.of(
        Stop.builder()
            .id("place-forhl")
            .name("Forest Hills")
            .build(),
        Stop.builder()
            .id("Malden")
            .name("place-maldn")
            .build()
    );

    @Test
    public void testGetMbtaRoutes() {
        var typeRef = new ParameterizedTypeReference<JsonApiResponse<Route>>(){};

        Mockito.when(fetchResourceService.getResources(eq(typeRef), any(URI.class), anyString(), any()))
            .thenReturn(Mono.just(ResponseEntity.ok(ROUTES)));

        webTestClient
            .get()
            .uri("/mbta/routes")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Route.class).isEqualTo(ROUTES);

        Mockito.verify(fetchResourceService).getResources(
            eq(typeRef),
            eq(URI.create("https://api-v3.mbta.com/routes?fields[route]=color,direction_destinations,text_color,long_name")),
            eq("routes"),
            any()
        );
    }

    @Test
    public void testGetMbtaStops() {
        var typeRef = new ParameterizedTypeReference<JsonApiResponse<Stop>>(){};

        Mockito.when(fetchResourceService.getResources(eq(typeRef), any(URI.class), anyString(), any()))
            .thenReturn(Mono.just(ResponseEntity.ok(STOPS)));

        webTestClient
            .get()
            .uri("/mbta/stops?routeId={routeId}&directionId={directionId}", "Orange", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Stop.class).isEqualTo(STOPS);

        Mockito.verify(fetchResourceService).getResources(
            eq(typeRef),
            eq(URI.create("https://api-v3.mbta.com/stops?filter[route]=Orange&filter[direction_id]=1&fields[stop]=name")),
            eq("routes/Orange/directions/1/stops"),
            any()
        );
    }

    @Test
    public void testGetMbtaStops_missingRouteId() {
        webTestClient
            .get()
            .uri("/mbta/stops?directionId={directionId}", 1)
            .exchange()
            .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(fetchResourceService);
    }

    @Test
    public void testGetMbtaStops_missingDirectionId() {
        webTestClient
            .get()
            .uri("/mbta/stops?routeId={routeId}", "Orange")
            .exchange()
            .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(fetchResourceService);
    }
}
