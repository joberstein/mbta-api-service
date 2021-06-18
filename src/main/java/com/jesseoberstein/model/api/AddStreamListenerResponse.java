package com.jesseoberstein.model.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddStreamListenerResponse {
    private final String streamId;
    private final String clientId;
}
