package com.example.itemservice.dto;

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
public class ItemResponse{
        String id;
        String sku;
        String name;
        String description;
        List<String> categories;
        BigDecimal price;
        int stock;
        boolean active;
        String url;
}