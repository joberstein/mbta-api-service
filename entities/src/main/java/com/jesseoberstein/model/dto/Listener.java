package com.jesseoberstein.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder(toBuilder = true)
public class Listener {
    private String id;
    private String token;
    private int directionId;
    private Duration ttl;
}
