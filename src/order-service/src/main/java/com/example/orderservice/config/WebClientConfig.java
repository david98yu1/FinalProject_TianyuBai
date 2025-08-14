package com.example.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient itemClient(
            WebClient.Builder builder,
            @Value("${item.service.base-url:http://localhost:9002}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }
}