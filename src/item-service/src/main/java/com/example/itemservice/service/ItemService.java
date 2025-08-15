package com.example.itemservice.service;

import com.example.itemservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.commonlib.dto.item.ItemDto;

public interface ItemService {
    ItemResponse create(ItemDto req);
    ItemResponse getById(String id);
    ItemResponse getBySku(String sku);
    Page<ItemResponse> search(String q, Pageable pageable);
    ItemResponse update(String id, UpdateItemRequest req);
    ItemResponse adjustStock(String id, int delta);
}