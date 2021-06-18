package com.jesseoberstein.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Data
@Builder(toBuilder = true)
public class Client {
    private String id;
    private String token;
    private int directionId;
    private Duration ttl;
    private ScheduledFuture<?> handleExpirationFuture;
}
