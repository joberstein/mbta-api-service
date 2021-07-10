package com.jesseoberstein.dao;

import com.jesseoberstein.model.db.PredictionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionDao extends CrudRepository<PredictionEntity, String> {

    List<PredictionEntity> findByRouteIdAndStopId(String routeId, String stopId);

    void deleteByRouteIdAndStopId(String routeId, String stopId);
}
