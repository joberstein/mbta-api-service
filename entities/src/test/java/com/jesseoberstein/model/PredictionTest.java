package com.jesseoberstein.model;

import com.jesseoberstein.model.mbta.Prediction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PredictionTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.systemDefault());

    @Test
    public void testGetTime_hasDepartureAndArrivalTimes() {
        var prediction = Prediction.builder()
            .departureTime(NOW)
            .arrivalTime(NOW.plusMinutes(5))
            .build();

        assertEquals(NOW.plusMinutes(5), prediction.getTime());
    }

    @Test
    public void testGetTime_missingDepartureTime() {
        var prediction = Prediction.builder()
            .arrivalTime(NOW)
            .build();

        assertEquals(NOW, prediction.getTime());
    }

    @Test
    public void testGetTime_missingArrivalTime() {
        var prediction = Prediction.builder()
            .departureTime(NOW)
            .build();

        assertEquals(NOW, prediction.getTime());
    }

    @Test
    public void testGetTime_missingDepartureAndArrivalTime() {
        var prediction = Prediction.builder().build();
        Assertions.assertNull(prediction.getTime());
    }

    @Test
    public void testGetDisplayText_hasStatus() {
        var prediction = Prediction.builder()
            .status("Status")
            .departureTime(NOW)
            .arrivalTime(NOW.plusMinutes(5))
            .build();

        assertEquals("Status", prediction.getDisplayText());
    }

    @Test
    public void testGetDisplayText_missingStatus() {
        var prediction = Prediction.builder()
            .departureTime(NOW)
            .build();

        assertEquals(NOW.format(DATE_TIME_FORMATTER), prediction.getDisplayText());
    }
}
