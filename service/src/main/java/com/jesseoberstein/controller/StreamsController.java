package com.jesseoberstein.controller;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import com.jesseoberstein.model.dto.Listener;
import com.jesseoberstein.model.dto.Stream;
import com.jesseoberstein.service.ConnectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/streams")
public class StreamsController {

    private final ConnectionService connectionService;

    @GetMapping
    public Collection<Stream> getStreams() {
        return connectionService.getConnections().values().stream()
            .map(Connection::toStream)
            .sorted(Comparator.comparing(Stream::getStartedAt).reversed())
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createStream(
        @RequestParam String routeId,
        @RequestParam String stopId,
        @RequestBody Listener listener
    ) {
        var client = Client.fromListener(listener);
        var connection = connectionService.open(routeId, stopId, client);
        return connection.getStreamId();
    }

    @GetMapping("/{streamId}")
    public ResponseEntity<Stream> getStream(@PathVariable String streamId) {
        var connection = connectionService.getConnections().getOrDefault(streamId, null);
        var optionalStream = Optional.ofNullable(connection).map(Connection::toStream);
        return ResponseEntity.of(optionalStream);
    }

    @DeleteMapping("/{streamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stopStream(@PathVariable String streamId) {
        connectionService.close(streamId);
    }
}
