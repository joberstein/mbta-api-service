package com.jesseoberstein.controller;

import com.jesseoberstein.model.mbta.*;
import com.jesseoberstein.service.FetchResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/mbta")
@RequiredArgsConstructor
public class MbtaApiController {

    private final FetchResourceService fetchResourceService;

    private final UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
        .scheme("https")
        .host("api-v3.mbta.com");

    private static final String FILTERED_ROUTE_FIELDS = String.join(",",
        "color", "direction_destinations", "text_color", "long_name"
    );

    private static final String FILTERED_STOP_FIELDS = String.join(",",
        "name"
    );

    @GetMapping("/routes")
    public Mono<ResponseEntity<List<Route>>> getRoutes() {
        URI uri = uriBuilder.cloneBuilder()
            .path("routes")
            .queryParam(QueryParam.FIELDS_ROUTE.getParamName(), FILTERED_ROUTE_FIELDS)
            .build()
            .toUri();

        var typeRef = new ParameterizedTypeReference<JsonApiResponse<Route>>(){};
        Function<Resource<Route>, Route> mapResourceToRoute = resource -> resource.getAttributes().withId(resource.getId());

        var routes = fetchResourceService.getResources(typeRef, uri, "routes", mapResourceToRoute);
        return routes;
    }

    @GetMapping("/stops")
    public Mono<ResponseEntity<List<Stop>>> getStops(@RequestParam String routeId, @RequestParam String directionId) {
        URI uri = uriBuilder.cloneBuilder()
            .path("stops")
            .queryParam(QueryParam.FILTER_ROUTE.getParamName(), routeId)
            .queryParam(QueryParam.FILTER_DIRECTION.getParamName(), directionId)
            .queryParam(QueryParam.FIELDS_STOP.getParamName(), FILTERED_STOP_FIELDS)
            .build()
            .toUri();

        String lastModifiedKey = String.join("/", "routes", routeId, "directions", directionId, "stops");
        var typeRef = new ParameterizedTypeReference<JsonApiResponse<Stop>>(){};
        Function<Resource<Stop>, Stop> mapResourceToStop = resource -> resource.getAttributes().withId(resource.getId());

        return fetchResourceService.getResources(typeRef, uri, lastModifiedKey, mapResourceToStop);
    }
}