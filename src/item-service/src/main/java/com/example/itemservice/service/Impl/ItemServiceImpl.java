package com.example.itemservice.service.Impl;

import com.example.itemservice.dto.ItemResponse;
import com.example.itemservice.dto.UpdateItemRequest;
import com.example.itemservice.entity.Item;
import com.example.itemservice.repository.ItemRepository;
import com.example.itemservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.example.commonlib.dto.item.ItemDto;

@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repo;

    private ItemResponse toResp(Item i) {
        return new ItemResponse(
                i.getId(), i.getSku(), i.getName(), i.getDescription(),
                i.getCategories(), i.getPrice(), i.getStock(), i.isActive(), i.getPictureUrl()
        );
    }

    @Override
    public ItemResponse create(ItemDto req) {
        repo.findBySku(req.getSku()).ifPresent(item -> { throw new IllegalArgumentException("sku already exists"); });
        Item item = Item.builder().sku(req.getSku())
                .name(req.getName())
                .description(req.getDescription())
                .categories(req.getCategories() != null ? new ArrayList<>(req.getCategories()) : new ArrayList<>())
                .price(req.getPrice())
                .stock(req.getStock())
                .active(req.isActive())
                .pictureUrl(req.getPictureUrl())
                .build();
        return toResp(repo.save(item));
    }

    @Override @Transactional(readOnly = true)
    public ItemResponse getById(String id) {
        Item i = repo.findById(id).orElseThrow(() -> new NoSuchElementException("item not found"));
        return toResp(i);
    }

    @Override @Transactional(readOnly = true)
    public ItemResponse getBySku(String sku) {
        Item i = repo.findBySku(sku).orElseThrow(() -> new NoSuchElementException("item not found"));
        return toResp(i);
    }

    @Override @Transactional(readOnly = true)
    public Page<ItemResponse> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return repo.findAll(pageable).map(this::toResp);
        }
        return repo.findByNameContainingIgnoreCase(q, pageable).map(this::toResp);
    }

    @Override
    public ItemResponse update(String id, UpdateItemRequest r) {
        Item i = repo.findById(id).orElseThrow(() -> new NoSuchElementException("item not found"));
        if (r.getName() != null)        i.setName(r.getName());
        if (r.getDescription() != null) i.setDescription(r.getDescription());
        if (r.getCategories() != null)  i.setCategories(new java.util.ArrayList<>(r.getCategories()));
        if (r.getPrice() != null)       i.setPrice(r.getPrice());
        if (r.getActive() != null)      i.setActive(r.getActive());
        i = repo.save(i);
        return toResp(i);
    }

    @Override
    public ItemResponse adjustStock(String id, int delta) {
        Item i = repo.findById(id).orElseThrow(() -> new NoSuchElementException("item not found"));
        int newStock = i.getStock() + delta;
        if (newStock < 0) throw new IllegalArgumentException("stock cannot go below 0");
        i.setStock(newStock);
        i = repo.save(i);
        return toResp(i);
    }

}
