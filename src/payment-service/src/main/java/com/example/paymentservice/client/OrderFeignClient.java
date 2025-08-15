package com.example.paymentservice.client;

import com.example.commonlib.dto.order.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "orderClient",
        url = "${order.service.base-url}",
        configuration = com.example.feign.OrderFeignConfig.class
)
public interface OrderFeignClient {
    @GetMapping("/orders/{id}")
    OrderResponse get(@PathVariable("id") Long id);

    @PostMapping("/orders/{id}/confirm")
    OrderResponse confirm(@PathVariable("id") Long id);

    @PostMapping("/orders/{id}/cancel")
    OrderResponse cancel(@PathVariable("id") Long id);
}
