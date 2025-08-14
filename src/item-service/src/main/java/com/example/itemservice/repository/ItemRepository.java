package com.example.itemservice.repository;

import com.example.itemservice.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {
    Optional<Item> findBySku(String sku);
    Page<Item> findByNameContainingIgnoreCase(String q, Pageable pageable);
}