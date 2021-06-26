package com.jesseoberstein.model;

import com.jesseoberstein.model.dto.Stream;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import reactor.core.Disposable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
@Builder(toBuilder = true)
public class Connection {

    private String routeId;
    private String stopId;
    private Disposable subscription;
    private ConcurrentMap<String, Client> listeners;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    private LocalDateTime startedAt = LocalDateTime.now();

    public String getStreamId() {
        return String.join(":", routeId, stopId);
    }

    public static Connection fromStream(Stream stream) {
        return Connection.builder()
            .routeId(stream.getRouteId())
            .stopId(stream.getStopId())
            .build();
    }

    public Stream toStream() {
        return Stream.builder()
            .id(this.getStreamId())
            .routeId(this.routeId)
            .stopId(this.stopId)
            .startedAt(this.startedAt)
            .build();
    }

    public static class ConnectionBuilder {

        public ConnectionBuilder listeners(ConcurrentMap<String, Client> listeners) {
            this.listeners = listeners;
            return this;
        }

        public ConnectionBuilder listeners(Map<String, Client> listeners) {
            this.listeners = new ConcurrentHashMap<>(listeners);
            return this;
        }
    }
}
