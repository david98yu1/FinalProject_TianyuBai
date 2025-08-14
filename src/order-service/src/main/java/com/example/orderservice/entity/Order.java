// Order.java
package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal total;

    private OffsetDateTime createdAt;

    @Version
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    public void addItem(OrderItem i) { i.setOrder(this); items.add(i); }
}
