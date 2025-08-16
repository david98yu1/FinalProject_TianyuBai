package com.example.eventstore.web;

import com.example.eventstore.model.OrderEventsByAccount;
import com.example.eventstore.model.OrderEventsByOrder;
import com.example.eventstore.repo.OrderEventsByAccountRepo;
import com.example.eventstore.repo.OrderEventsByOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final OrderEventsByOrderRepo byOrderRepo;
    private final OrderEventsByAccountRepo byAccountRepo;

    @GetMapping("/orders/{orderId}")
    public List<OrderEventsByOrder> byOrder(@PathVariable Long orderId) {
        return byOrderRepo.findByOrderId(orderId);
    }

    @GetMapping("/accounts/{accountId}")
    public List<OrderEventsByAccount> byAccount(@PathVariable Long accountId) {
        return byAccountRepo.findByAccountId(accountId);
    }
}
