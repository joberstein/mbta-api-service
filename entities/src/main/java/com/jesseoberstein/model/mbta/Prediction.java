package com.jesseoberstein.model.mbta;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class Prediction {

    private String id;
    private String routeId;
    private String stopId;
    private int directionId;
    private String status;
    private ZonedDateTime arrivalTime;
    private ZonedDateTime departureTime;

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
