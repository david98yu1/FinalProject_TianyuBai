package com.example.orderservice.client;

import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ItemFeignConfig {
    @Bean
    public RequestInterceptor auth(ServiceTokenProvider tokens) {
        return template -> template.header("Authorization", "Bearer " + tokens.get());
    }

    // Optional: on 401, refresh then let service retry the call once
    @Bean
    public ErrorDecoder errorDecoder(ServiceTokenProvider tokens) {
        var defaultDecoder = new feign.codec.ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (response.status() == 401) {
                tokens.refresh(); // refresh for the next attempt in your service layer
            }
            return defaultDecoder.decode(methodKey, response);
        };
    }

    // Optional: small retry policy
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 500, 1); // backoff ms, max period ms, max attempts
    }
}
