package com.example.orderservice.repository;
import com.example.orderservice.entity.OrderItem;

public interface OrderItemRepository extends org.springframework.data.jpa.repository.JpaRepository<OrderItem, Long> {}
