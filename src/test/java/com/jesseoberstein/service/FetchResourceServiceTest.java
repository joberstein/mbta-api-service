package com.jesseoberstein.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesseoberstein.config.WebClientConfig;
import com.jesseoberstein.model.mbta.JsonApiResponse;
import com.jesseoberstein.model.mbta.Resource;
import com.jesseoberstein.model.mbta.Route;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@JsonTest
@Import({WebClientConfig.class})
public class FetchResourceServiceTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebClient webClient;

    FetchResourceService service;

    private MockWebServer mockWebServer;

    private static final String TEST_URL_PATH = "/test-mbta-url";
    private static final String LAST_MODIFIED = "Wed, 21 Oct 2015 07:28:00 GMT";

    private static final Route TEST_ROUTE_1 = Route.builder()
        .id("test-route-1")
        .longName("My Test Route")
        .color("#000000")
        .textColor("#FFFFFF")
        .directionDestinations(new String[]{"Back", "Forth"})
        .build();

    private static final Route TEST_ROUTE_2 = Route.builder()
        .id("test-route-2")
        .longName("My Test Route 2")
        .color("#0F0F0F")
        .textColor("#F0F0F0")
        .directionDestinations(new String[]{"Away", "Far"})
        .build();

    private MockResponse mockJsonApiResponse;

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        service = new FetchResourceService(webClient, "test-api-key");

        var resources = Stream.of(TEST_ROUTE_1, TEST_ROUTE_2)
            .map(route -> Resource.<Route>builder()
                .id(route.getId())
                .attributes(route.withId(null))
                .type("route")
                .build())
            .collect(Collectors.toList());

        var response = new JsonApiResponse<Route>();
        response.setData(resources);

        mockJsonApiResponse = new MockResponse()
            .setResponseCode(HttpStatus.SC_OK)
            .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .setHeader(HttpHeaders.LAST_MODIFIED, LAST_MODIFIED)
            .setBody(objectMapper.writeValueAsString(response));
    }

    @AfterEach
    public void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetResources() {
        mockWebServer.enqueue(mockJsonApiResponse);

        ParameterizedTypeReference<JsonApiResponse<Route>> typeRef = new ParameterizedTypeReference<>() {};
        Function<Resource<Route>, Route> mapResourceToRoute = resource -> resource.getAttributes().withId(resource.getId());
        var url = mockWebServer.url(TEST_URL_PATH);

        var actual = service.getResources(typeRef, url.uri(), "routes", mapResourceToRoute);
        var expected = List.of(TEST_ROUTE_1, TEST_ROUTE_2);

        assertEquals(expected, actual.block().getBody());
    }

    @Test
    public void testGetResources_lastModifiedHeader() throws InterruptedException {
        ParameterizedTypeReference<JsonApiResponse<Route>> typeRef = new ParameterizedTypeReference<>() {};
        Function<Resource<Route>, Route> mapResourceToRoute = resource -> resource.getAttributes().withId(resource.getId());
        var url = mockWebServer.url(TEST_URL_PATH);
        var lastModifiedKey = "routes";

        // After the first response, save the 'Last-Modified' header
        mockWebServer.enqueue(mockJsonApiResponse);
        service.getResources(typeRef, url.uri(), lastModifiedKey, mapResourceToRoute).block();
        mockWebServer.takeRequest();

        // After the second request, verify that 'If-Modified-Since' header matches
        mockWebServer.enqueue(mockJsonApiResponse);
        service.getResources(typeRef, url.uri(), lastModifiedKey, mapResourceToRoute).block();
        var request = mockWebServer.takeRequest();

        var ifModifiedSince = request.getHeaders().get(HttpHeaders.IF_MODIFIED_SINCE);
        assertEquals(LAST_MODIFIED, ifModifiedSince);
    }

    @Test
    public void testGetResources_notModified() {
        var mockResponse = new MockResponse()
            .setResponseCode(HttpStatus.SC_NOT_MODIFIED)
            .setBody("");

        mockWebServer.enqueue(mockResponse);

        ParameterizedTypeReference<JsonApiResponse<Route>> typeRef = new ParameterizedTypeReference<>() {};
        Function<Resource<Route>, Route> mapResourceToRoute = resource -> resource.getAttributes().withId(resource.getId());
        var url = mockWebServer.url(TEST_URL_PATH);

        var actual = service.getResources(typeRef, url.uri(), "routes", mapResourceToRoute);
        assertNull(actual.block().getBody());
    }
}
