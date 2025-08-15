package com.example.orderservice.service.Impl;

import com.example.orderservice.client.ItemFeignClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.commonlib.dto.order.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import com.example.commonlib.dto.item.ItemDto;
import com.example.commonlib.dto.item.InventoryAdjustRequest;
import com.example.commonlib.dto.order.OrderItemDto;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
    private final ItemFeignClient itemClient;

    // Minimal shape matching Item Service response
    static class ItemResp {
        public String id, sku, name, pictureUrl;
        public java.math.BigDecimal price; public int stock; public boolean active;
    }

    @Override
    public OrderResponse create(CreateOrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        var order = Order.builder()
                .accountId(req.getAccountId())
                .status(OrderStatus.PENDING)
                .createdAt(java.time.OffsetDateTime.now())
                .total(java.math.BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (var r : req.getItems()) {
            String sku = r.getSku();
            int qty = r.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Quantity must be > 0 for SKU " + sku);

            ItemDto item = itemClient.getBySku(sku);

            if (item.isActive()) throw new IllegalArgumentException("item not available: " + r.getSku());
            if (item.getStock() < r.getQuantity()) throw new IllegalArgumentException("insufficient stock: " + r.getSku());

            var lineTotal = item.getPrice().multiply(java.math.BigDecimal.valueOf(r.getQuantity()));
            total = total.add(lineTotal);

            order.addItem(OrderItem.builder()
                    .itemId(item.getId()).sku(item.getSku()).name(item.getName()).pictureUrl(item.getPictureUrl())
                    .unitPrice(item.getPrice()).quantity(r.getQuantity()).lineTotal(lineTotal)
                    .build());
        }

        order.setTotal(total);
        order = orderRepo.save(order);

        // decrement stock
        for (var oi : order.getItems()) {
            itemClient.adjustInventory(
                    oi.getItemId(),
                    new InventoryAdjustRequest(-oi.getQuantity())
            );
        }

        order.setStatus(OrderStatus.PENDING);
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
            itemClient.adjustInventory(
                    oi.getItemId(),
                    new InventoryAdjustRequest(oi.getQuantity())
            );
        }
        o.setStatus(OrderStatus.CANCELED);
        return toResponse(o);
    }

    @Override
    public OrderResponse confirm(Long id) {
        var o = orderRepo.findById(id).orElseThrow(() -> new NoSuchElementException("order not found"));
        if (o.getStatus() != OrderStatus.PENDING) return toResponse(o);
        o.setStatus(OrderStatus.CONFIRMED);
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
