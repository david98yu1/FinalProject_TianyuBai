// OrderItem.java
package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String itemId;     // from Item Service
    private String sku;
    private String name;
    private String pictureUrl;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;

    private int quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal lineTotal;
}
