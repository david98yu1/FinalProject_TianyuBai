package com.example.orderservice.client;

import com.example.commonlib.dto.item.InventoryAdjustRequest;
import com.example.commonlib.dto.item.ItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "itemClient",
        url = "${item.service.base-url}",
        configuration = ItemFeignConfig.class
)
public interface ItemFeignClient {

    @GetMapping("/items/sku/{sku}")
    ItemDto getBySku(@PathVariable("sku") String sku);

    @PostMapping("/items/{id}/inventory/adjust")
    ItemDto adjustInventory(@PathVariable("id") String itemId,
                            @RequestBody InventoryAdjustRequest body);
}
