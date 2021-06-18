package com.jesseoberstein.dao;

import com.jesseoberstein.config.DatabaseConfig;
import com.jesseoberstein.model.mbta.Prediction;
import com.jesseoberstein.service.PredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@Import({DatabaseConfig.class})
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class PredictionDaoTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @Autowired
    private PredictionDao predictionDao;

    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MILLIS);

    private final Prediction PREDICTION_1 = Prediction.builder()
        .id("1")
        .routeId("Orange")
        .stopId("place-forhl")
        .directionId(0)
        .departureTime(NOW)
        .arrivalTime(NOW.plusMinutes(5))
        .build();

    private final Prediction PREDICTION_2 = Prediction.builder()
        .id("2")
        .routeId("Orange")
        .stopId("place-forhl")
        .directionId(1)
        .status("Status")
        .arrivalTime(NOW)
        .build();

    private final Prediction PREDICTION_3 = Prediction.builder()
        .id("3")
        .routeId("Orange")
        .stopId("place-maldn")
        .directionId(0)
        .departureTime(NOW.plusMinutes(15))
        .build();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setup() {
        var predictions = List.of(PREDICTION_1, PREDICTION_2, PREDICTION_3);
        predictionDao.saveAll(predictions);
    }

    @Test
    public void testFindByRouteIdAndStopId() {
        var predictions = predictionDao.findByRouteIdAndStopId("Orange", "place-forhl")
            .stream()
            .sorted(Comparator.comparing(Prediction::getId))
            .collect(Collectors.toList());

        assertEquals(List.of(PREDICTION_1, PREDICTION_2), predictions);
    }

    @Test
    public void testDeleteAllById() {
        predictionDao.deleteAllById(List.of("1", "3"));
        assertEquals(List.of(PREDICTION_2), predictionDao.findAll());
    }

    @Test
    public void testDeleteAll() {
        predictionDao.deleteAll();
        assertEquals(Collections.emptyList(), predictionDao.findAll());
    }

    @Test
    public void testDeleteByRouteIdAndStopId() {
        predictionDao.deleteByRouteIdAndStopId("Orange", "place-forhl");
        assertEquals(List.of(PREDICTION_3), predictionDao.findAll());
    }
}
