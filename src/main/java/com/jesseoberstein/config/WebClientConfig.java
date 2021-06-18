package com.jesseoberstein.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public Jackson2JsonDecoder jsonDecoder(ObjectMapper mapper) {
        return new Jackson2JsonDecoder(mapper);
    }

    @Bean
    public WebClient webClient(Jackson2JsonDecoder decoder) {
        return WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .jackson2JsonDecoder(decoder))
            .build();
    }
}
