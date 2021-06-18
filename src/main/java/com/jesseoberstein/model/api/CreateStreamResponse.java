package com.jesseoberstein.model.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateStreamResponse {
    private final String streamId;
}
