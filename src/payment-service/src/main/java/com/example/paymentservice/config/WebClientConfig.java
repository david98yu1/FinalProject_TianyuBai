package com.example.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("orderWebClient")
    public WebClient orderWebClient(WebClient.Builder builder,
                                 @Value("${order.service.base-url:http://localhost:9003}") String baseUrl) {
        return builder.baseUrl(baseUrl)
                .defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }
}
