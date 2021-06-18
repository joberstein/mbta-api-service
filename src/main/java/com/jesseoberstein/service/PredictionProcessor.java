package com.jesseoberstein.service;

import com.jesseoberstein.model.mbta.Prediction;
import com.jesseoberstein.model.mbta.Resource;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PredictionProcessor {

    private final PredictionService predictionService;
    private final MessageBuildingService messageBuildingService;
    private final QueueingService queueingService;


    public void processEvent(ServerSentEvent<List<Resource<Prediction>>> event, String routeId, String stopId) {
        var resources = Optional.ofNullable(event.data()).orElse(Collections.emptyList());

        if (resources.isEmpty()) {
            return;
        }

        var eventType = Optional.ofNullable(event.event()).orElse("");

        if ("remove".equals(eventType)) {
            var predictionIds = resources.stream()
                .map(Resource::getId)
                .collect(Collectors.toList());

            predictionService.delete(predictionIds);
        } else {
            var predictions = resources.stream()
                .map(resource -> resource.getAttributes().toBuilder()
                    .id(resource.getId())
                    .routeId(routeId)
                    .stopId(stopId)
                    .build())
                .collect(Collectors.toList());

            predictionService.upsert(predictions);
        }
    }

    public void broadcastPredictions(String routeId, String stopId) {
        predictionService.get(routeId, stopId)
            .parallelStream()
            .collect(Collectors.groupingBy(Prediction::getDirectionId))
            .entrySet()
            .parallelStream()
            .map(entry -> {
                var topic = String.join(".", routeId, stopId, String.valueOf(entry.getKey()));
                return messageBuildingService.build(topic, routeId, stopId, entry.getKey(), entry.getValue());
            })
            .forEach(queueingService::add);
    }
}
