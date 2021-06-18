package com.jesseoberstein.model.mbta;

import lombok.Data;

import java.util.List;

@Data
public class JsonApiResponse<T> {
    private List<Resource<T>> data;
}
