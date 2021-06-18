package com.jesseoberstein.model;

import lombok.Builder;
import lombok.Data;
import reactor.core.Disposable;

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

    public String getStreamId() {
        return String.join(":", routeId, stopId);
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
