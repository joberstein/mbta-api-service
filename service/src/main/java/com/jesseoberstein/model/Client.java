package com.jesseoberstein.model;

import com.jesseoberstein.model.dto.Listener;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Data
@Builder(toBuilder = true)
public class Client {

    private String id;
    private int directionId;
    private Duration ttl;
    private String token;
    private ScheduledFuture<?> handleExpirationFuture;

    public static Client fromListener(Listener listener) {
        return Client.builder()
            .id(listener.getId())
            .token(listener.getToken())
            .directionId(listener.getDirectionId())
            .ttl(listener.getTtl())
            .build();
    }

    public Listener toListener() {
        return Listener.builder()
            .id(this.id)
            .token("[hidden]")
            .directionId(this.directionId)
            .ttl(this.ttl)
            .build();
    }
}
