package com.example.authservice.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountFeignConfig {
    private final ServiceTokenIssuer issuer;

    @Bean
    public RequestInterceptor accountAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + issuer.mintSystemToken());
    }
}
