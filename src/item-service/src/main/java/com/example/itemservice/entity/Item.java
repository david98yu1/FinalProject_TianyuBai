package com.example.itemservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Document("items")
@NoArgsConstructor                 // () -> new Item()
@AllArgsConstructor
@Builder
public class Item {
    @Id private String id;          // Mongo ObjectId as String is fine
    @Indexed(unique = true) private String sku;
    @Indexed private String name;
    private String description;
    private List<String> categories;
    private BigDecimal price;       // use BigDecimal for money
    private int stock;              // on-hand quantity
    private boolean active;         // for soft-hiding
    private String pictureUrl;
}