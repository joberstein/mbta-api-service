package com.jesseoberstein.model.mbta;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@With
@Builder(toBuilder = true)
public class Route {
    private String id;
    private String color;
    private String[] directionDestinations;
    private String longName;
    private String textColor;
}
