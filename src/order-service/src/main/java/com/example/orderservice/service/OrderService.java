package com.example.orderservice.service;


import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;

public interface OrderService {
    OrderResponse create(CreateOrderRequest req);
    OrderResponse get(Long id);
    OrderResponse cancel(Long id);
    OrderResponse confirm(Long id);
}
