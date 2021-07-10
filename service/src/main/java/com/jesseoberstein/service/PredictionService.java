package com.jesseoberstein.service;

import com.jesseoberstein.dao.PredictionDao;
import com.jesseoberstein.model.db.PredictionEntity;
import com.jesseoberstein.model.mbta.Prediction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionDao predictionDao;

    public List<Prediction> get(String routeId, String stopId) {
        var entities = predictionDao.findByRouteIdAndStopId(routeId, stopId);
        return fromEntities(entities);
    }

    public Iterable<Prediction> getAll() {
        return fromEntities(predictionDao.findAll());
    }

    public void upsert(List<Prediction> predictions) {
        var entities = toEntities(predictions);
        predictionDao.saveAll(entities);
    }

    public void delete(List<String> ids) {
        predictionDao.deleteAllById(ids);
    }

    public void delete(String routeId, String stopId) {
        predictionDao.deleteByRouteIdAndStopId(routeId, stopId);
    }

    public void deleteAll() {
        predictionDao.deleteAll();
    }

    private List<Prediction> fromEntities(Iterable<PredictionEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
            .map(PredictionEntity::toDto)
            .collect(Collectors.toList());
    }

    private List<PredictionEntity> toEntities(List<Prediction> predictions) {
        return predictions.stream()
            .map(PredictionEntity::fromDto)
            .collect(Collectors.toList());
    }
}
