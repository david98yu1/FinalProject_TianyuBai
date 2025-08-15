// com/example/feign/order/OrderFeignConfig.java
package com.example.feign;

import com.example.paymentservice.client.ServiceTokenProvider;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OrderFeignConfig {
    @Bean
    public RequestInterceptor orderAuthInterceptor(ServiceTokenProvider tokens) {
        return template -> template.header("Authorization", "Bearer " + tokens.get());
    }
}
