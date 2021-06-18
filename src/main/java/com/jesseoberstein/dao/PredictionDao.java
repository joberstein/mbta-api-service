package com.jesseoberstein.dao;

import com.jesseoberstein.model.mbta.Prediction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionDao extends CrudRepository<Prediction, String> {

    List<Prediction> findByRouteIdAndStopId(String routeId, String stopId);

    void deleteByRouteIdAndStopId(String routeId, String stopId);
}
