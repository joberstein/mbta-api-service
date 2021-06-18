package com.jesseoberstein.controller;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.api.AddStreamListenerResponse;
import com.jesseoberstein.model.api.CreateStreamRequest;
import com.jesseoberstein.model.api.CreateStreamResponse;
import com.jesseoberstein.service.ConnectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/streams")
public class StreamsController {

    private final ConnectionService connectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateStreamResponse createPredictionStream(@RequestBody CreateStreamRequest request) {
        var connection = connectionService.open(request.getConnection(), request.getClient());

        return CreateStreamResponse.builder()
            .streamId(connection.getStreamId())
            .build();
    }

    @PostMapping("/{streamId}/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public AddStreamListenerResponse addListener(@PathVariable String streamId, @RequestBody Client client) {
        var connection = connectionService.connect(streamId, client);

        return AddStreamListenerResponse.builder()
            .streamId(connection.getStreamId())
            .clientId(client.getId())
            .build();
    }

    @DeleteMapping("/{streamId}/clients/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeListener(@PathVariable String streamId, @PathVariable String clientId) {
        connectionService.disconnect(streamId, clientId);
    }
}
