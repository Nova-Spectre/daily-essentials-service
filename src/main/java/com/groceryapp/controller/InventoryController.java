package com.groceryapp.controller;

import com.groceryapp.dto.request.inventory.InventoryRequest;
import com.groceryapp.dto.response.common.GenericResponseWrapper;
import com.groceryapp.dto.response.inventory.InventoryResponse;
import com.groceryapp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping()
    public ResponseEntity<GenericResponseWrapper<InventoryResponse>> addInventory(
            @RequestBody InventoryRequest request) {
        log.info("Adding inventory: {}", request);
        InventoryResponse response = inventoryService.addInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponseWrapper.success(response));
    }

    @GetMapping()
    public ResponseEntity<GenericResponseWrapper<List<InventoryResponse>>> getInventory() throws ExecutionException, InterruptedException {
        log.info("Fetching all inventory");
        CompletableFuture<List<InventoryResponse>> inventoryFuture = inventoryService.getAllInventory();
        List<InventoryResponse> inventory = inventoryFuture.get();
        return ResponseEntity.ok(GenericResponseWrapper.success(inventory));
    }

}
