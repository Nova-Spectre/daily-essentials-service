package com.groceryapp.service;

import com.groceryapp.dto.request.inventory.InventoryRequest;
import com.groceryapp.dto.response.inventory.InventoryResponse;

import java.util.List;

public interface InventoryService {
    InventoryResponse addInventory(InventoryRequest request);

    List<InventoryResponse> getAllInventory();
}
