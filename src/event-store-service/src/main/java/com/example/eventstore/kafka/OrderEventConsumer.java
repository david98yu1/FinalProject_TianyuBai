package com.example.eventstore.kafka;

import com.example.eventstore.model.*;
import com.example.eventstore.model.OrderEventsByOrder;
import com.example.eventstore.repo.OrderEventsByAccountRepo;
import com.example.eventstore.repo.OrderEventsByOrderRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderEventsByOrderRepo byOrderRepo;
    private final OrderEventsByAccountRepo byAccountRepo;
    private final ObjectMapper om = new ObjectMapper();

    // Listens to both topics; you can split into two methods if you prefer.
    @KafkaListener(topics = {"order-events", "payment-events"})
    public void handle(String raw) throws Exception {
        JsonNode j = om.readTree(raw);

        // Expected JSON:
        // {
        //   "eventType":"ORDER_CREATED",        // or PAYMENT_CAPTURED, ...
        //   "orderId":123,
        //   "accountId":456,
        //   "at":"2025-08-15T05:20:10Z",
        //   "data": { ... arbitrary payload ... }
        // }

        String type = j.path("eventType").asText();
        long orderId = j.path("orderId").asLong();
        long accountId = j.path("accountId").asLong();
        Instant at = parseInstant(j.path("at").asText());
        String payload = om.writeValueAsString(j.path("data"));

        byOrderRepo.save(new OrderEventsByOrder(orderId, at, type, payload));
        byAccountRepo.save(new OrderEventsByAccount(accountId, at, orderId, type, payload));
    }

    private Instant parseInstant(String s) {
        try { return Instant.parse(s); } catch (Exception e) { return Instant.now(); }
    }
}
