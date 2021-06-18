package com.jesseoberstein.model.mbta;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@Document("predictions")
public class Prediction {

    String id;
    String routeId;
    String stopId;
    int directionId;
    String status;
    ZonedDateTime arrivalTime;
    ZonedDateTime departureTime;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    public ZonedDateTime getTime() {
        return Optional.ofNullable(this.arrivalTime)
                .or(() -> Optional.ofNullable(this.departureTime))
                .orElse(null);
    }

    public String getDisplayText() {
        return Optional.ofNullable(this.status)
            .orElseGet(() -> Optional.ofNullable(this.getTime())
                .map(t -> t.toLocalTime().format(TIME_FORMAT))
                .orElse(""));
    }
}
