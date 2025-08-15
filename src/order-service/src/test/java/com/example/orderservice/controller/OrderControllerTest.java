package com.example.orderservice.controller;

import com.example.commonlib.dto.order.OrderItemDto;
import com.example.commonlib.dto.order.OrderResponse;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.security.JwtService;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false) // disable filters for slice tests
class OrderControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean OrderService service;
    @MockitoBean JwtService jwtService; // satisfy JwtAuthFilter construction

    private OrderResponse sampleResponse(long id, String status) {
        List<OrderItemDto> items = List.of(new OrderItemDto("SKU1", "Item 1", "pic", new BigDecimal("10.00"), new BigDecimal("20.00"), 2));
        return new OrderResponse(id, status, new BigDecimal("20.00"), items);
    }

    @Test
    @DisplayName("POST /orders -> 201 Created")
    void create_order() throws Exception {
        when(service.create(any(CreateOrderRequest.class))).thenReturn(sampleResponse(100L, "PENDING"));

        mvc.perform(MockMvcRequestBuilders.post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"accountId": 1, "items":[{"sku":"SKU1","quantity":2}]}
                        """))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(100))
           .andExpect(jsonPath("$.status").value("PENDING"))
           .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    @DisplayName("GET /orders/{id} -> 200")
    void get_order() throws Exception {
        when(service.get(7L)).thenReturn(sampleResponse(7L, "PENDING"));

        mvc.perform(MockMvcRequestBuilders.get("/orders/{id}", 7L))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel -> 200")
    void cancel_order() throws Exception {
        when(service.cancel(9L)).thenReturn(sampleResponse(9L, "CANCELED"));

        mvc.perform(MockMvcRequestBuilders.post("/orders/{id}/cancel", 9L))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    @DisplayName("POST /orders/{id}/confirm -> 200")
    void confirm_order() throws Exception {
        when(service.confirm(9L)).thenReturn(sampleResponse(9L, "CONFIRMED"));

        mvc.perform(MockMvcRequestBuilders.post("/orders/{id}/confirm", 9L))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}