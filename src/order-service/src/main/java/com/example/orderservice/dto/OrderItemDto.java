// OrderItemDto.java
package com.example.orderservice.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private String sku, name, pictureUrl;
    private BigDecimal unitPrice, lineTotal;
    private int quantity;
}
