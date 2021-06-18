package com.jesseoberstein.service;

import com.jesseoberstein.model.mbta.Header;
import com.jesseoberstein.model.mbta.Prediction;
import com.jesseoberstein.model.mbta.QueryParam;
import com.jesseoberstein.model.mbta.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.Disposable;

import java.net.URI;
import java.util.List;

@Service
@Log4j2
@AllArgsConstructor
public class PredictionStreamingService {

    private final WebClient webClient;
    private final PredictionProcessor eventProcessor;

    private final ParameterizedTypeReference<ServerSentEvent<List<Resource<Prediction>>>> SSE_TYPE = new ParameterizedTypeReference<>() {};
    private final String PREDICTION_FIELDS = String.join(",",
        "arrival_time",
        "departure_time",
        "direction_id",
        "status"
    );

    @Value("${MBTA_API_KEY}")
    private final String mbtaApiKey;

    public Disposable start(String routeId, String stopId) {
        log.info("Starting stream for {}:{}", routeId, stopId);

        return webClient
            .get()
            .uri(uriBuilder -> this.buildUri(uriBuilder, routeId, stopId))
            .header(Header.X_API_KEY.getHeaderName(), mbtaApiKey)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(SSE_TYPE)
            .subscribe(
                e -> eventProcessor.processEvent(e, routeId, stopId),
                Throwable::printStackTrace);
    }

    URI buildUri(UriBuilder uriBuilder, String routeId, String stopId) {
        return uriBuilder
            .scheme("https")
            .host("api-v3.mbta.com")
            .path("predictions")
            .queryParam(QueryParam.FILTER_ROUTE.getParamName(), routeId)
            .queryParam(QueryParam.FILTER_STOP.getParamName(), stopId)
            .queryParam(QueryParam.FIELDS_PREDICTION.getParamName(), PREDICTION_FIELDS)
            .build();
    }
}
