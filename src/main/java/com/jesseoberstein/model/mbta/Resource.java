package com.jesseoberstein.model.mbta;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Resource<T> {
    private T attributes;
    private String id;
    private String type;
}
