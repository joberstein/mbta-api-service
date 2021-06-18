package com.jesseoberstein.service;

import com.jesseoberstein.dao.PredictionDao;
import com.jesseoberstein.model.mbta.Prediction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionDao predictionDao;

    public List<Prediction> get(String routeId, String stopId) {
        return predictionDao.findByRouteIdAndStopId(routeId, stopId);
    }

    public Iterable<Prediction> getAll() {
        return predictionDao.findAll();
    }

    public void upsert(List<Prediction> predictions) {
        predictionDao.saveAll(predictions);
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
}
