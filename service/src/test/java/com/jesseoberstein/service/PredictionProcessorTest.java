package com.jesseoberstein.service;

import com.google.firebase.messaging.Message;
import com.jesseoberstein.model.PredictionNotification;
import com.jesseoberstein.model.mbta.Prediction;
import com.jesseoberstein.model.mbta.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PredictionProcessorTest {

    @Mock
    private QueueingService queueingService;

    @Mock
    private PredictionService predictionService;

    @Mock
    private MessageBuildingService messageBuildingService;

    @InjectMocks
    private PredictionProcessor processor;

    private final String ROUTE_ID = "Red";
    private final String STOP_ID = "place-davis";
    private final int DIRECTION_ID = 1;
    private final String STATUS = "Status";
    private final String PREDICTION_ID = "prediction-id";
    private final ZonedDateTime ARRIVAL_TIME = ZonedDateTime.now(ZoneId.systemDefault());
    private final ZonedDateTime DEPARTURE_TIME = ARRIVAL_TIME.plusMinutes(15);

    @BeforeEach
    public void setup() {
        Mockito.lenient()
            .doNothing()
            .when(predictionService).delete(anyList());

        Mockito.lenient()
            .when(messageBuildingService.build(anyString(), anyString(), anyString(), anyInt(), anyList()))
            .thenReturn(mock(Message.class));

        Mockito.lenient()
            .when(messageBuildingService.buildMessageData(anyString(), anyString(), anyInt(), anyList()))
            .thenReturn(PredictionNotification.builder().build());

        Mockito.lenient().doNothing()
            .when(queueingService).add(any(Message.class));
    }

    @Test
    public void testProcess_addEvent() {
        Resource<Prediction> resource = Resource.<Prediction>builder()
            .id(PREDICTION_ID)
            .type("prediction")
            .attributes(Prediction.builder()
                .arrivalTime(ARRIVAL_TIME)
                .departureTime(DEPARTURE_TIME)
                .directionId(DIRECTION_ID)
                .status(STATUS)
                .build())
            .build();

        var addEvent = ServerSentEvent.<List<Resource<Prediction>>>builder()
            .id("event-id")
            .event("add")
            .data(List.of(resource))
            .build();

        var expectedPrediction = Prediction.builder()
            .id(PREDICTION_ID)
            .routeId(ROUTE_ID)
            .stopId(STOP_ID)
            .directionId(DIRECTION_ID)
            .arrivalTime(ARRIVAL_TIME)
            .departureTime(DEPARTURE_TIME)
            .status(STATUS)
            .build();

        processor.processEvent(addEvent, ROUTE_ID, STOP_ID);
        Mockito.verify(predictionService).upsert(List.of(expectedPrediction));
    }

    @Test
    public void testProcess_removeEvent() {
        Resource<Prediction> resource = Resource.<Prediction>builder()
            .id(PREDICTION_ID)
            .type("prediction")
            .build();

        var removeEvent = ServerSentEvent.<List<Resource<Prediction>>>builder()
            .id("event-id")
            .event("remove")
            .data(List.of(resource))
            .build();

        processor.processEvent(removeEvent, ROUTE_ID, STOP_ID);
        Mockito.verify(predictionService).delete(List.of(PREDICTION_ID));
    }

    @Test
    public void testProcess_eventMissingResources() {
        var emptyEvent = ServerSentEvent.<List<Resource<Prediction>>>builder()
            .data(Collections.emptyList())
            .build();

        processor.processEvent(emptyEvent, ROUTE_ID, STOP_ID);
        Mockito.verifyNoInteractions(predictionService);
    }

    @Test
    public void testBroadcast() {
        var prediction1 = Prediction.builder().id("1").directionId(1).build();
        var prediction2 = Prediction.builder().id("2").directionId(0).build();
        var prediction3 = Prediction.builder().id("3").directionId(1).build();

        Mockito.when(predictionService.get(ROUTE_ID, STOP_ID))
            .thenReturn(List.of(prediction1, prediction2, prediction3));

        processor.broadcastPredictions(ROUTE_ID, STOP_ID);

        String topicDir0 = String.join(".", ROUTE_ID, STOP_ID, "0");
        Mockito.verify(messageBuildingService).build(topicDir0, ROUTE_ID, STOP_ID, 0, List.of(prediction2));

        String topicDir1 = String.join(".", ROUTE_ID, STOP_ID, "1");
        Mockito.verify(messageBuildingService).build(topicDir1, ROUTE_ID, STOP_ID, 1, List.of(prediction1, prediction3));

        Mockito.verify(queueingService, times(2)).add(any(Message.class));
    }

    @Test
    public void testBroadcast_missingPredictions() {
        Mockito.when(predictionService.get(ROUTE_ID, STOP_ID))
            .thenReturn(Collections.emptyList());

        processor.broadcastPredictions(ROUTE_ID, STOP_ID);

        Mockito.verifyNoInteractions(messageBuildingService);
        Mockito.verifyNoInteractions(queueingService);
    }
}
