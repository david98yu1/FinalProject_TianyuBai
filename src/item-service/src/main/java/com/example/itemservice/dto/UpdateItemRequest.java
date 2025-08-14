package com.example.itemservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    String name;
    String description;
    List<@NotBlank String> categories;
    @DecimalMin(value = "0.00")
    BigDecimal price;
    Boolean active;
    String url;
}
