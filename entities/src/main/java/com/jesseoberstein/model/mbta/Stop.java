package com.jesseoberstein.model.mbta;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@Data
@With
@Builder(toBuilder = true)
public class Stop {
    private String id;
    private String name;
}
