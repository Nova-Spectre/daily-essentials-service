package com.groceryapp.service.serviceImpl;

import com.groceryapp.constant.common.ErrorCode;
import com.groceryapp.constant.inventory.InventoryStatus;
import com.groceryapp.dto.request.inventory.InventoryRequest;
import com.groceryapp.dto.response.inventory.InventoryResponse;
import com.groceryapp.exception.ServiceException;
import com.groceryapp.model.Brand;
import com.groceryapp.model.Category;
import com.groceryapp.model.Inventory;
import com.groceryapp.model.Item;
import com.groceryapp.repository.BrandRepository;
import com.groceryapp.repository.CategoryRepository;
import com.groceryapp.repository.InventoryRepository;
import com.groceryapp.repository.ItemRepository;
import com.groceryapp.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Adds inventory for a given item, creating the brand, category, and item if they do not already exist.
     * Updates the inventory quantity accordingly.
     *
     * @param request The inventory request containing brand, category, price, and quantity details.
     * @return The updated inventory response after adding the inventory.
     * @throws ServiceException If there is an invalid request or insufficient inventory.
     */
    @Override
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request) {
        Brand brand = getBrandByName(request.getBrand());
        Category category = getCategoryByName(request.getCategory());
        Item item = getOrCreateItem(brand, category, request.getPrice());
        return updateInventory(item, request.getQuantity(), true);
    }

    /**
     * Retrieves all inventory items Asynchronously, mapping them to a list of InventoryResponse objects.
     *
     * @return A list of inventory responses.
     */
    @Override
    @Async
    public CompletableFuture<List<InventoryResponse>> getAllInventory() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        List<InventoryResponse> inventoryResponses = inventoryList.stream()
                .map(this::mapToInventoryResponse)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(inventoryResponses);
    }


    private Brand getBrandByName(String brandName) {
        return brandRepository.findByName(brandName).orElseGet(() -> {
            Brand newBrand = new Brand();
            newBrand.setName(brandName);
            return brandRepository.save(newBrand);
        });
    }

    private Category getCategoryByName(String categoryName) {
        return categoryRepository.findByName(categoryName).orElseGet(() -> {
            Category newCategory = new Category();
            newCategory.setName(categoryName);
            return categoryRepository.save(newCategory);
        });
    }

    private Item getOrCreateItem(Brand brand, Category category, Double price) {
        Optional<Item> existingItem = itemRepository.findByCategoryAndBrand(category, brand);

        if (existingItem.isPresent()) {
            Item item = existingItem.get();
            if (price != null && !Objects.equals(item.getPrice(), price)) {
                item.setPrice(price);
                return itemRepository.save(item);
            }
            return item;
        } else {
            Item newItem = new Item();
            newItem.setBrand(brand);
            newItem.setCategory(category);
            newItem.setPrice(price);
            return itemRepository.save(newItem);
        }
    }

    private InventoryResponse updateInventory(Item item, Integer quantityChange, boolean isAddition) {
        Inventory inventory = inventoryRepository.findByItem(item).orElseGet(() -> {
            Inventory newInventory = new Inventory();
            newInventory.setItem(item);
            newInventory.setQuantity(0);
            newInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
            return newInventory;
        });

        int currentQuantity = inventory.getQuantity();
        int newQuantity;

        if (isAddition) {
            if (quantityChange <= 0) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST, "Quantity to add must be greater than zero");
            }
            newQuantity = currentQuantity + quantityChange;
        } else {
            if (quantityChange <= 0) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST, "Quantity to remove must be greater than zero");
            }
            if (currentQuantity < quantityChange) {
                throw new ServiceException(ErrorCode.INSUFFICIENT_INVENTORY,
                        "Not enough inventory. Current: " + currentQuantity + ", Requested: " + quantityChange);
            }
            newQuantity = currentQuantity - quantityChange;
        }

        inventory.setQuantity(newQuantity);
        inventory.updateStatus();
        Inventory savedInventory = inventoryRepository.save(inventory);

        return mapToInventoryResponse(savedInventory);
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder().brand(inventory.getItem().getBrand().getName())
                .category(inventory.getItem().getCategory().getName()).quantity(inventory.getQuantity())
                .status(inventory.getStatus()).build();
    }
}