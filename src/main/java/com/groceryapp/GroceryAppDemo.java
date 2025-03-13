package com.groceryapp;

import com.groceryapp.dto.request.inventory.InventoryRequest;
import com.groceryapp.dto.response.common.PaginatedResponse;
import com.groceryapp.dto.response.inventory.InventoryResponse;
import com.groceryapp.dto.response.search.SearchResultItem;
import com.groceryapp.service.InventoryService;
import com.groceryapp.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroceryAppDemo implements CommandLineRunner {

    private final InventoryService inventoryService;
    private final SearchService searchService;

    @Override
    public void run(String... args) {
        log.info("Starting Grocery App Demo");

        populateInventory();

        displayAllInventory();

        performSearchDemonstrations();

        log.info("Grocery App Demo completed");
    }

    private void populateInventory() {
        log.info("=== Adding Items and Inventory ===");

        addItem("Amul", "Milk", 100.0, 10);
        addItem("Amul", "Curd", 50.0, 5);
        addItem("Nestle", "Milk", 60.0, 5);
        addItem("Nestle", "Curd", 90.0, 10);
        addItem("Britannia", "Bread", 40.0, 15);
        addItem("Modern", "Bread", 35.0, 20);
        addItem("Tata", "Salt", 20.0, 50);
        addItem("Aashirvaad", "Flour", 250.0, 30);

        addInventory("Amul", "Milk", 10);
    }

    private void addItem(String brand, String category, Double price, Integer quantity) {
        InventoryRequest request = new InventoryRequest();
        request.setBrand(brand);
        request.setCategory(category);
        request.setPrice(price);
        request.setQuantity(quantity);

        InventoryResponse response = inventoryService.addInventory(request);
        log.info("Added item: {} - {} (Price: {}, Quantity: {})", response.getBrand(), response.getCategory(), price,
                response.getQuantity());
    }

    private void addInventory(String brand, String category, Integer quantity) {
        InventoryRequest request = new InventoryRequest();
        request.setBrand(brand);
        request.setCategory(category);
        request.setQuantity(quantity);

        InventoryResponse response = inventoryService.addInventory(request);
        log.info("Updated inventory: {} - {} (Quantity: {})", response.getBrand(), response.getCategory(),
                response.getQuantity());
    }

    private void displayAllInventory() {
        log.info("=== Current Inventory ===");
        List<InventoryResponse> inventory = inventoryService.getAllInventory();

        for (InventoryResponse item : inventory) {
            log.info("{} -> {} -> {} (Status: {})", item.getBrand(), item.getCategory(), item.getQuantity(),
                    item.getStatus());
        }
    }

    private void performSearchDemonstrations() {
        log.info("=== Search by Brand ===");
        searchByBrand("Nestle");

        log.info("=== Search by Category ===");
        searchByCategory("Milk");

        log.info("=== Search by Category with Price Sorting (Descending) ===");
        searchByCategoryWithPriceSorting("Milk", Sort.Direction.DESC);

        log.info("=== Search by Price Range ===");
        searchByPriceRange(70.0, 100.0);

        log.info("=== Search with Multiple Criteria (Category and Price Range) ===");
        searchByCategoryAndPriceRange("Milk", 70.0, 100.0, Sort.Direction.DESC);
    }

    private void searchByBrand(String brand) {
        PaginatedResponse<SearchResultItem> results = searchService.searchItems(Collections.singletonList(brand), null,
                null, null, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));

        displaySearchResults(results);
    }

    private void searchByCategory(String category) {
        PaginatedResponse<SearchResultItem> results = searchService.searchItems(null,
                Collections.singletonList(category), null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));

        displaySearchResults(results);
    }

    private void searchByCategoryWithPriceSorting(String category, Sort.Direction direction) {
        PaginatedResponse<SearchResultItem> results = searchService.searchItems(null,
                Collections.singletonList(category), null, null, PageRequest.of(0, 10, Sort.by(direction, "price")));

        displaySearchResults(results);
    }

    private void searchByPriceRange(Double minPrice, Double maxPrice) {
        PaginatedResponse<SearchResultItem> results = searchService.searchItems(null, null, minPrice, maxPrice,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));

        displaySearchResults(results);
    }

    private void searchByCategoryAndPriceRange(String category, Double minPrice, Double maxPrice,
            Sort.Direction direction) {
        PaginatedResponse<SearchResultItem> results = searchService.searchItems(null,
                Collections.singletonList(category), minPrice, maxPrice,
                PageRequest.of(0, 10, Sort.by(direction, "price")));

        displaySearchResults(results);
    }

    private void displaySearchResults(PaginatedResponse<SearchResultItem> response) {
        log.info("Found {} results (Page {} of {})", response.getTotalResults(), response.getCurrentPage() + 1,
                response.getTotalPages());

        for (SearchResultItem item : response.getResults()) {
            log.info("{}, {}, {} (Price: {})", item.getBrand(), item.getCategory(), item.getQuantity(),
                    item.getPrice());
        }
    }
}