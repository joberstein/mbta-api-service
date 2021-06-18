package com.jesseoberstein.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PredictionStreamingServiceTest {

    @Mock
    private PredictionProcessor eventProcessor;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private PredictionStreamingService service;

    private UriBuilder uriBuilder;

    @BeforeEach
    public void setup() {
        uriBuilder = UriComponentsBuilder.newInstance();
    }

    @Test
    public void testBuildUri() {
        var uri = service.buildUri(uriBuilder, "Orange", "place-forhl");
        var queryParams = String.join("&",
            String.join("=", encode("filter[route]", UTF_8), "Orange"),
                String.join("=", encode("filter[stop]", UTF_8), "place-forhl"),
                String.join("=", encode("fields[prediction]", UTF_8), "arrival_time,departure_time,direction_id,status")
        );

        var expected = "https://api-v3.mbta.com/predictions?" + queryParams;
        assertEquals(expected, uri.toString());
    }
}
