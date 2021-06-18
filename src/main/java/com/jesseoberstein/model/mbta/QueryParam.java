package com.jesseoberstein.model.mbta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QueryParam {
    FILTER_ROUTE("filter[route]"),
    FILTER_DIRECTION("filter[direction_id]"),
    FILTER_STOP("filter[stop]"),
    FIELDS_PREDICTION("fields[prediction]"),
    FIELDS_ROUTE("fields[route]"),
    FIELDS_STOP("fields[stop]");

    private final String paramName;
}
