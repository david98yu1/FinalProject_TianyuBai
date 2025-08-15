package com.example.orderservice.service.Impl;

import com.example.commonlib.dto.item.ItemDto;
import com.example.commonlib.dto.order.OrderResponse;
import com.example.orderservice.client.ItemFeignClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock ItemFeignClient itemClient;
    @Mock OrderRepository orderRepo;

    @InjectMocks OrderServiceImpl service;

    @Captor ArgumentCaptor<Order> orderCaptor;

    private ItemDto mockItem(String id, String sku, String name, BigDecimal price, boolean active, int stock, String pic) {
        ItemDto dto = mock(ItemDto.class);
        when(dto.getId()).thenReturn(id);
        when(dto.getSku()).thenReturn(sku);
        when(dto.getName()).thenReturn(name);
        when(dto.getPrice()).thenReturn(price);
        when(dto.isActive()).thenReturn(active);
        when(dto.getStock()).thenReturn(stock);
        when(dto.getPictureUrl()).thenReturn(pic);
        return dto;
    }

    @Test
    @DisplayName("create: builds order lines, totals, saves, decrements inventory, returns PENDING response")
    void create_success() {
        // Given request with 2 items
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(123L);
            return o;
        });

        CreateOrderRequest req = new CreateOrderRequest();
        req.setAccountId(42L);
        req.setItems(List.of(
                new OrderItemRequest("SKU1", 2),
                new OrderItemRequest("SKU2", 1)
        ));

        when(itemClient.getBySku("SKU1")).thenReturn(
                item("i1","SKU1","Item One", new BigDecimal("10.00"), true, 5, "pic1"));
        when(itemClient.getBySku("SKU2")).thenReturn(
                item("i2","SKU2","Item Two", new BigDecimal("3.50"), true, 100, "pic2"));
        // When
        OrderResponse res = service.create(req);

        // Then: repository save called once
        verify(orderRepo).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertThat(saved.getAccountId()).isEqualTo(42L);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getTotal()).isEqualByComparingTo(new BigDecimal("23.50"));

        // Item inventory adjusted (-qty) for each line
        verify(itemClient).adjustInventory(eq("i1"), any());
        verify(itemClient).adjustInventory(eq("i2"), any());

        // Response mirrors totals/status
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(123L);
        assertThat(res.getStatus()).isEqualTo("PENDING");
        assertThat(res.getTotal()).isEqualByComparingTo(new BigDecimal("23.50"));
        assertThat(res.getItems()).hasSize(2);
    }

    private ItemDto item(String id, String sku, String name,
                         BigDecimal price, boolean active, int stock, String pic) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setSku(sku);
        dto.setName(name);
        dto.setPrice(price);
        dto.setActive(active);
        dto.setStock(stock);
        dto.setPictureUrl(pic);
        return dto;
    }

    @Test
    @DisplayName("create: rejects empty order and non-positive quantity")
    void create_validation_errors() {
        // empty
        CreateOrderRequest empty = new CreateOrderRequest();
        empty.setAccountId(1L);
        assertThatThrownBy(() -> service.create(empty))
                .isInstanceOf(IllegalArgumentException.class);

        // qty <= 0
        CreateOrderRequest badQty = new CreateOrderRequest();
        badQty.setAccountId(1L);
        badQty.setItems(List.of(new OrderItemRequest("SKU", 0)));
        assertThatThrownBy(() -> service.create(badQty))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: rejects inactive or out-of-stock items")
    void create_reject_unavailable() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setAccountId(1L);
        req.setItems(List.of(new OrderItemRequest("SKU", 3)));

        when(itemClient.getBySku("SKU"))
                .thenReturn(item("x","SKU","X", new BigDecimal("5.00"), /*active=*/false, 10, "pic"));
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class);

        when(itemClient.getBySku("SKU"))
                .thenReturn(item("x","SKU","X", new BigDecimal("5.00"), /*active=*/true, 2, "pic"));
        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("get: returns response when found; throws when missing")
    void get_found_and_missing() {
        Order o = Order.builder()
                .id(9L).accountId(7L).status(OrderStatus.PENDING).createdAt(OffsetDateTime.now())
                .total(new BigDecimal("1.00"))
                .items(List.of(OrderItem.builder().itemId("i").sku("S").name("N").unitPrice(new BigDecimal("1.00")).quantity(1).lineTotal(new BigDecimal("1.00")).build()))
                .build();

        when(orderRepo.findById(9L)).thenReturn(Optional.of(o));
        OrderResponse res = service.get(9L);
        assertThat(res.getId()).isEqualTo(9L);
        assertThat(res.getStatus()).isEqualTo("PENDING");

        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("cancel: idempotent; first time restocks, sets CANCELED")
    void cancel_flow() {
        Order o = Order.builder()
                .id(10L).accountId(1L).status(OrderStatus.PENDING)
                .items(List.of(
                        OrderItem.builder().itemId("i1").sku("S1").name("N1").unitPrice(new BigDecimal("2")).quantity(2).lineTotal(new BigDecimal("4")).build(),
                        OrderItem.builder().itemId("i2").sku("S2").name("N2").unitPrice(new BigDecimal("3")).quantity(1).lineTotal(new BigDecimal("3")).build()
                ))
                .total(new BigDecimal("7"))
                .build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(o));

        OrderResponse res1 = service.cancel(10L);
        assertThat(res1.getStatus()).isEqualTo("CANCELED");
        verify(itemClient).adjustInventory(eq("i1"), any());
        verify(itemClient).adjustInventory(eq("i2"), any());

        // second time: should be idempotent (no more restock)
        reset(itemClient);
        OrderResponse res2 = service.cancel(10L);
        assertThat(res2.getStatus()).isEqualTo("CANCELED");
        verify(itemClient, never()).adjustInventory(anyString(), any());
    }

    @Test
    @DisplayName("confirm: only transitions PENDING -> CONFIRMED")
    void confirm_flow() {
        Order pending = Order.builder().id(11L).status(OrderStatus.PENDING).items(List.of()).total(BigDecimal.ZERO).build();
        when(orderRepo.findById(11L)).thenReturn(Optional.of(pending));

        OrderResponse r1 = service.confirm(11L);
        assertThat(r1.getStatus()).isEqualTo("CONFIRMED");

        Order canceled = Order.builder().id(12L).status(OrderStatus.CANCELED).items(List.of()).total(BigDecimal.ZERO).build();
        when(orderRepo.findById(12L)).thenReturn(Optional.of(canceled));
        OrderResponse r2 = service.confirm(12L);
        assertThat(r2.getStatus()).isEqualTo("CANCELED");
    }
}