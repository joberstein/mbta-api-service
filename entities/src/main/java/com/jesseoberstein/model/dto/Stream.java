package com.jesseoberstein.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class Stream {
    private String id;
    private String routeId;
    private String stopId;

    @EqualsAndHashCode.Exclude
    private LocalDateTime startedAt;
}
