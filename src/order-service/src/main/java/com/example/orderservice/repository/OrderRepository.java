package com.example.orderservice.repository;
import com.example.orderservice.entity.Order;

public interface OrderRepository extends org.springframework.data.jpa.repository.JpaRepository<Order, Long> {}