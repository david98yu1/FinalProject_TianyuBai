package com.example.commonlib.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    public String id;
    @NotBlank
    String sku;
    @NotBlank String name;
    String description;
    List<@NotBlank String> categories;
    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal price;
    @Min(0) int stock;
    boolean active;
    String url;
    String pictureUrl;
}