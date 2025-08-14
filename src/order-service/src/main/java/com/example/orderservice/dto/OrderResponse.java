// OrderResponse.java  (simplified)
package com.example.orderservice.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal total;
    private List<OrderItemDto> items;
}
