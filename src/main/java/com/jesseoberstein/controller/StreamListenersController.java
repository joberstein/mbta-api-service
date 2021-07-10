package com.jesseoberstein.controller;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import com.jesseoberstein.model.dto.Listener;
import com.jesseoberstein.service.ConnectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/streams/{streamId}/listeners")
public class StreamListenersController {

    private final ConnectionService connectionService;

    @GetMapping
    public ResponseEntity<Collection<Listener>> getListeners(@PathVariable String streamId) {
        var connection = connectionService.getConnections().getOrDefault(streamId, null);

        Optional<Collection<Listener>> optionalListeners = Optional.ofNullable(connection)
            .map(Connection::getListeners)
            .map(listeners -> listeners.values().stream()
                .map(Client::toListener)
                .sorted(Comparator.comparing(Listener::getTtl))
                .collect(Collectors.toUnmodifiableList()));

        return ResponseEntity.of(optionalListeners);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addListener(@PathVariable String streamId, @RequestBody Listener listener) {
        var client = Client.fromListener(listener);
        connectionService.connect(streamId, client);
    }

    @DeleteMapping("/{listenerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeListener(@PathVariable String streamId, @PathVariable String listenerId) {
        connectionService.disconnect(streamId, listenerId);
    }
}
