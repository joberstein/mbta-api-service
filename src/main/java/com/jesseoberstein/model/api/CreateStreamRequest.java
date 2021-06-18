package com.jesseoberstein.model.api;

import com.jesseoberstein.model.Client;
import com.jesseoberstein.model.Connection;
import lombok.Data;

@Data
public class CreateStreamRequest {
    private Connection connection;
    private Client client;
}
