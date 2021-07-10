package com.jesseoberstein.model.mbta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Header {
    X_API_KEY("x-api-key");

    private final String headerName;
}
