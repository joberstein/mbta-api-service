package com.jesseoberstein.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.client.model.Indexes;
import com.jesseoberstein.model.mbta.Prediction;

@ChangeLog
public class PredictionsChangeLog {

    @ChangeSet(order = "1", id = "predictions-1", author = "joberstein", systemVersion = "1.0.0")
    public void createPredictionsCollection(MongockTemplate mongoTemplate) {
        var compoundIndex = Indexes.compoundIndex(
            Indexes.ascending("routeId"),
            Indexes.ascending("stopId"),
            Indexes.ascending("directionId")
        );

        mongoTemplate
            .createCollection(Prediction.class)
            .createIndex(compoundIndex);
    }
}
