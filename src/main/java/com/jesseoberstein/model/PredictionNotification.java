package com.jesseoberstein.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PredictionNotification {
    private String route;
    private String stop;
    private String direction;
    private List<String> displayTimes;
    private LocalDateTime created;
}
