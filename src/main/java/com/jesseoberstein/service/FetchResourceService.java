package com.jesseoberstein.service;

import com.jesseoberstein.model.mbta.Header;
import com.jesseoberstein.model.mbta.JsonApiResponse;
import com.jesseoberstein.model.mbta.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class FetchResourceService {

    private final WebClient webClient;

    @Value("${MBTA_API_KEY}")
    private final String mbtaApiKey;

    private final Map<String, Instant> lastModifiedMap = new HashMap<>();

    private static final ZoneId GMT = ZoneId.of("GMT");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
        .withZone(GMT);

    public <T> Mono<ResponseEntity<List<T>>> getResources(
        ParameterizedTypeReference<JsonApiResponse<T>> typeRef,
        URI uri,
        String lastModifiedKey,
        Function<Resource<T>, T> mapResource
    ) {
        return webClient
            .get()
            .uri(uri)
            .header(Header.X_API_KEY.getHeaderName(), mbtaApiKey)
            .header(HttpHeaders.IF_MODIFIED_SINCE, this.getIfModifiedSince(lastModifiedKey))
            .retrieve()
            .toEntity(typeRef)
            .doOnSuccess(response -> this.onReceiveResources(response, lastModifiedKey))
            .map(response -> Optional.ofNullable(response.getBody()).map(JsonApiResponse::getData))
            .map(data -> data.map(resources -> resources.parallelStream()
                .map(mapResource)
                .collect(Collectors.toList())))
            .map(optionalResources -> optionalResources
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build()));
    }

    private <T> void onReceiveResources(ResponseEntity<JsonApiResponse<T>> response, String lastModifiedKey) {
        log.info(response.getBody());
        Instant lastModified = Instant.ofEpochMilli(response.getHeaders().getLastModified());
        this.saveLastModified(lastModified, lastModifiedKey);
    }

    private String getIfModifiedSince(String lastModifiedKey) {
        return Optional.ofNullable(lastModifiedMap.get(lastModifiedKey))
            .map(lastModified -> ZonedDateTime.ofInstant(lastModified, GMT))
            .map(DATE_TIME_FORMATTER::format)
            .orElse("");
    }

    private void saveLastModified(Instant lastModified, String key) {
        lastModifiedMap.compute(key, (k, cached) ->
            Optional.ofNullable(cached)
                .filter(lastModified::isBefore)
                .orElse(lastModified)
        );
    }
}
