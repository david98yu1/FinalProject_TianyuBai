// OrderItemRequest.java
package com.example.orderservice.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {
    @NotBlank private String sku;
    @Min(1) private int quantity;
}
