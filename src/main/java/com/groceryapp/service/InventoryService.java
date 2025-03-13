package com.groceryapp.service;

import com.groceryapp.dto.request.inventory.InventoryRequest;
import com.groceryapp.dto.response.inventory.InventoryResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InventoryService {
    InventoryResponse addInventory(InventoryRequest request);

    CompletableFuture<List<InventoryResponse>> getAllInventory();
}
