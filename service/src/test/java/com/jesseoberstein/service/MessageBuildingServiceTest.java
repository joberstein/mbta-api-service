package com.jesseoberstein.service;

import com.jesseoberstein.model.mbta.Prediction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class MessageBuildingServiceTest {

    @Mock
    private ObjectMappingService mappingService;

    @InjectMocks
    private MessageBuildingService service;

    private final String ROUTE_ID = "Red";
    private final String STOP_ID = "place-davis";
    private final int DIRECTION_ID = 1;
    private final String STATUS = "Status";
    private final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.systemDefault());

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    @Test
    public void testBuildMessageData() {
        var predictionOld = Prediction.builder()
            .id("p1")
            .arrivalTime(NOW.minusMinutes(15))
            .build();

        var predictionFirst = Prediction.builder()
            .id("p1")
            .arrivalTime(NOW.plusMinutes(15))
            .departureTime(NOW.plusMinutes(25))
            .build();

        var predictionMiddle = Prediction.builder()
            .id("p1")
            .status(STATUS)
            .arrivalTime(NOW.plusMinutes(20))
            .build();

        var predictionLast = Prediction.builder()
            .id("p1")
            .departureTime(NOW.plusMinutes(30))
            .build();

        var predictions = List.of(predictionLast, predictionOld, predictionFirst, predictionMiddle);
        var data = service.buildMessageData(ROUTE_ID, STOP_ID, DIRECTION_ID, predictions);

        var predictionTimes = List.of(
            NOW.plusMinutes(15).format(TIME_FORMAT),
            STATUS,
            NOW.plusMinutes(30).format(TIME_FORMAT)
        );

        Assertions.assertEquals(ROUTE_ID, data.getRoute());
        Assertions.assertEquals(STOP_ID, data.getStop());
        Assertions.assertEquals(String.valueOf(DIRECTION_ID), data.getDirection());
        Assertions.assertEquals(predictionTimes, data.getDisplayTimes());
        Assertions.assertNotNull(data.getCreated());
    }
}
