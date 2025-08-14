package com.example.itemservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateItemRequest {
    @NotBlank String sku;
    @NotBlank String name;
    String description;
    List<@NotBlank String> categories;
    @NotNull @DecimalMin(value = "0.00")
    BigDecimal price;
    @Min(0) int stock;
    boolean active;
    String url;
    String pictureUrl;
}
