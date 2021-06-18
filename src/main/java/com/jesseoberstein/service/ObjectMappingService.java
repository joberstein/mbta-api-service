package com.jesseoberstein.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class ObjectMappingService {

    private final ObjectMapper objectMapper;

    public <T> String stringify(T data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("Could not serialize data: {}", data);
            return "";
        }
    }

    <T> T parse(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("Could not deserialize data: {}", json);
            return null;
        }
    }
}
