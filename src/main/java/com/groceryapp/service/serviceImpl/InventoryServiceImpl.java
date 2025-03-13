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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request) {
        log.info("Processing inventory request: {}", request);

        Brand brand = getBrandByName(request.getBrand());

        Category category = getCategoryByName(request.getCategory());

        Item item = getOrCreateItem(brand, category, request.getPrice());

        return updateInventory(item, request.getQuantity(), true);

    }

    @Override
    public List<InventoryResponse> getAllInventory() {
        log.info("Fetching all inventory items");

        List<Inventory> inventoryList = inventoryRepository.findAll();

        return inventoryList.stream().map(this::mapToInventoryResponse).collect(Collectors.toList());
    }

    private Brand getBrandByName(String brandName) {
        return brandRepository.findByName(brandName).orElseGet(() -> {
            log.info("Creating new brand: {}", brandName);
            Brand newBrand = new Brand();
            newBrand.setName(brandName);
            return brandRepository.save(newBrand);
        });
    }

    private Category getCategoryByName(String categoryName) {
        return categoryRepository.findByName(categoryName).orElseGet(() -> {
            log.info("Creating new category: {}", categoryName);
            Category newCategory = new Category();
            newCategory.setName(categoryName);
            return categoryRepository.save(newCategory);
        });
    }

    private Item getOrCreateItem(Brand brand, Category category, Double price) {
        Optional<Item> existingItem = itemRepository.findByCategoryAndBrand(category, brand);

        if (existingItem.isPresent()) {
            Item item = existingItem.get();

            if (!item.getPrice().equals(price)) {
                log.info("Updating price for item {}. Old price: {}, New price: {}", item.getId(), item.getPrice(),
                        price);
                item.setPrice(price);
                return itemRepository.save(item);
            }
            return item;
        } else {
            log.info("Creating new item for brand: {} and category: {}", brand.getName(), category.getName());
            Item newItem = new Item();
            newItem.setBrand(brand);
            newItem.setCategory(category);
            newItem.setPrice(price);
            return itemRepository.save(newItem);
        }
    }

    private InventoryResponse updateInventory(Item item, Integer quantityChange, boolean isAddition) {
        Inventory inventory = inventoryRepository.findByItem(item).orElseGet(() -> {
            log.info("Creating new inventory for item: {}", item.getId());
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

        log.info("Inventory updated. Item: {}, New quantity: {}, Status: {}", item.getId(),
                savedInventory.getQuantity(), savedInventory.getStatus());

        return mapToInventoryResponse(savedInventory);
    }

    private InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder().brand(inventory.getItem().getBrand().getName())
                .category(inventory.getItem().getCategory().getName()).quantity(inventory.getQuantity())
                .status(inventory.getStatus()).build();
    }
}
