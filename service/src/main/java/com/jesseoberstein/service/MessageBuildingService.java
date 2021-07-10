package com.jesseoberstein.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidFcmOptions;
import com.google.firebase.messaging.Message;
import com.jesseoberstein.model.PredictionNotification;
import com.jesseoberstein.model.mbta.Prediction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageBuildingService {

    private final ObjectMappingService mappingService;

    public Message build(String topic, String routeId, String stopId, int directionId, List<Prediction> predictions) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss"));

        var fcmOptions = AndroidFcmOptions.builder()
            .setAnalyticsLabel("alert_" + now)
            .build();

        var androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setFcmOptions(fcmOptions)
            .build();

        var data = buildMessageData(routeId, stopId, directionId, predictions);

        return Message.builder()
            .setTopic(topic)
            .putData("data", mappingService.stringify(data))
            .setAndroidConfig(androidConfig)
            .build();
    }

    PredictionNotification buildMessageData(String routeId, String stopId, int directionId, List<Prediction> predictions) {
        var predictionTimes = predictions.stream()
            .filter(prediction -> prediction.getTime() != null || prediction.getStatus() != null)
            .filter(prediction -> prediction.getTime().isAfter(ZonedDateTime.now(ZoneId.systemDefault())))
            .sorted(Comparator.comparing(Prediction::getTime))
            .map(Prediction::getDisplayText)
            .collect(Collectors.toList());

        return PredictionNotification.builder()
            .route(routeId)
            .stop(stopId)
            .direction(String.valueOf(directionId))
            .displayTimes(predictionTimes)
            .created(LocalDateTime.now())
            .build();
    }
}
