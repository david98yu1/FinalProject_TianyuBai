package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.commonlib.dto.order.OrderResponse;
import com.example.orderservice.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@lombok.RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public OrderResponse create(@jakarta.validation.Valid @RequestBody CreateOrderRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) { return service.cancel(id); }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Long id) { return service.confirm(id); }
}

