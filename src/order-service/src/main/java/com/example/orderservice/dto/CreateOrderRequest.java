// CreateOrderRequest.java
package com.example.orderservice.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    @NotNull private Long accountId;
    @NotEmpty private List<OrderItemRequest> items;
}
