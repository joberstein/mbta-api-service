package com.jesseoberstein.model.db;

import com.jesseoberstein.model.mbta.Prediction;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Document("predictions")
public class PredictionEntity {
    private String id;
    private String routeId;
    private String stopId;
    private int directionId;
    private String status;
    private ZonedDateTime arrivalTime;
    private ZonedDateTime departureTime;

    public Prediction toDto() {
        return Prediction.builder()
            .id(id)
            .routeId(routeId)
            .stopId(stopId)
            .directionId(directionId)
            .status(status)
            .arrivalTime(arrivalTime)
            .departureTime(departureTime)
            .build();
    }

    public static PredictionEntity fromDto(Prediction prediction) {
        return PredictionEntity.builder()
            .id(prediction.getId())
            .routeId(prediction.getRouteId())
            .stopId(prediction.getStopId())
            .directionId(prediction.getDirectionId())
            .status(prediction.getStatus())
            .arrivalTime(prediction.getArrivalTime())
            .departureTime(prediction.getDepartureTime())
            .build();
    }
}
