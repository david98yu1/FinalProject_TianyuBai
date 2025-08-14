package com.example.orderservice.service.Impl;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@lombok.RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final org.springframework.web.reactive.function.client.WebClient itemClient;

    // Minimal shape matching Item Service response
    static class ItemResp {
        public String id, sku, name, pictureUrl;
        public java.math.BigDecimal price; public int stock; public boolean active;
    }

    @Override
    public OrderResponse create(CreateOrderRequest req) {
        var order = Order.builder()
                .accountId(req.getAccountId())
                .status(OrderStatus.PENDING)
                .createdAt(java.time.OffsetDateTime.now())
                .total(java.math.BigDecimal.ZERO)
                .build();

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (var r : req.getItems()) {
            ItemResp item = itemClient.get()
                    .uri("/items/sku/{sku}", r.getSku())
                    .retrieve().bodyToMono(ItemResp.class).block();

            if (item == null || !item.active) throw new IllegalArgumentException("item not available: " + r.getSku());
            if (item.stock < r.getQuantity()) throw new IllegalArgumentException("insufficient stock: " + r.getSku());

            var lineTotal = item.price.multiply(java.math.BigDecimal.valueOf(r.getQuantity()));
            total = total.add(lineTotal);

            order.addItem(OrderItem.builder()
                    .itemId(item.id).sku(item.sku).name(item.name).pictureUrl(item.pictureUrl)
                    .unitPrice(item.price).quantity(r.getQuantity()).lineTotal(lineTotal)
                    .build());
        }

        order.setTotal(total);
        order = orderRepo.save(order);

        // decrement stock
        for (var oi : order.getItems()) {
            itemClient.post()
                    .uri("/items/{id}/inventory/adjust", oi.getItemId())
                    .bodyValue(java.util.Map.of("delta", -oi.getQuantity()))
                    .retrieve().toBodilessEntity().block();
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return toResponse(order);
    }

    @Override @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        var o = orderRepo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("order not found"));
        // items are LAZY; access to initialize inside tx
        o.getItems().size();
        return toResponse(o);
    }

    @Override
    public OrderResponse cancel(Long id) {
        var o = orderRepo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("order not found"));
        if (o.getStatus() == OrderStatus.CANCELED) return toResponse(o);
        // restock
        for (var oi : o.getItems()) {
            itemClient.post().uri("/items/{id}/inventory/adjust", oi.getItemId())
                    .bodyValue(java.util.Map.of("delta", oi.getQuantity()))
                    .retrieve().toBodilessEntity().block();
        }
        o.setStatus(OrderStatus.CANCELED);
        return toResponse(o);
    }

    private OrderResponse toResponse(Order o) {
        var items = o.getItems().stream().map(oi -> new OrderItemDto(
                oi.getSku(), oi.getName(), oi.getPictureUrl(), oi.getUnitPrice(),
                oi.getLineTotal(), oi.getQuantity()
                )).toList();
        return new OrderResponse(o.getId(), o.getStatus().name(), o.getTotal(), items);
    }
}
