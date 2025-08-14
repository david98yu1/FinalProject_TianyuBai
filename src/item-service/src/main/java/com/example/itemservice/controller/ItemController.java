package com.example.itemservice.controller;

import com.example.itemservice.dto.*;
import com.example.itemservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody CreateItemRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/sku/{sku}")
    public ItemResponse getBySku(@PathVariable String sku) {
        return service.getBySku(sku);
    }

    @GetMapping
    public Page<ItemResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(q, PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable String id, @Valid @RequestBody UpdateItemRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/inventory/adjust")
    public ItemResponse adjust(@PathVariable String id, @Valid @RequestBody AdjustStockRequest req) {
        return service.adjustStock(id, req.getDelta());
    }
}
