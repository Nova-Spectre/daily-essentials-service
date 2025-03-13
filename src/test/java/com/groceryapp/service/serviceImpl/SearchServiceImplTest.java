package com.groceryapp.service.serviceImpl;

import com.groceryapp.dto.response.common.PaginatedResponse;
import com.groceryapp.dto.response.search.SearchResultItem;
import com.groceryapp.model.Brand;
import com.groceryapp.model.Category;
import com.groceryapp.model.Inventory;
import com.groceryapp.model.Item;
import com.groceryapp.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private List<Inventory> testInventory;
    private Inventory appleInventory;
    private Inventory orangeInventory;
    private Inventory milkInventory;

    @BeforeEach
    void setUp() {
        Brand appleBrand = new Brand();
        appleBrand.setId(1L);
        appleBrand.setName("Apple");

        Brand orangeBrand = new Brand();
        orangeBrand.setId(2L);
        orangeBrand.setName("Orange");

        Brand dairyBrand = new Brand();
        dairyBrand.setId(3L);
        dairyBrand.setName("Dairy Farm");

        Category fruitCategory = new Category();
        fruitCategory.setId(1L);
        fruitCategory.setName("Fruit");

        Category dairyCategory = new Category();
        dairyCategory.setId(2L);
        dairyCategory.setName("Dairy");

        Item appleItem = new Item();
        appleItem.setId(1L);
        appleItem.setBrand(appleBrand);
        appleItem.setCategory(fruitCategory);
        appleItem.setPrice(2.99);

        Item orangeItem = new Item();
        orangeItem.setId(2L);
        orangeItem.setBrand(orangeBrand);
        orangeItem.setCategory(fruitCategory);
        orangeItem.setPrice(3.49);

        Item milkItem = new Item();
        milkItem.setId(3L);
        milkItem.setBrand(dairyBrand);
        milkItem.setCategory(dairyCategory);
        milkItem.setPrice(4.99);

        appleInventory = new Inventory();
        appleInventory.setId(1L);
        appleInventory.setItem(appleItem);
        appleInventory.setQuantity(100);

        orangeInventory = new Inventory();
        orangeInventory.setId(2L);
        orangeInventory.setItem(orangeItem);
        orangeInventory.setQuantity(75);

        milkInventory = new Inventory();
        milkInventory.setId(3L);
        milkInventory.setItem(milkItem);
        milkInventory.setQuantity(50);

        testInventory = Arrays.asList(appleInventory, orangeInventory, milkInventory);
    }

    @Test
    void testSearchItems_NoFilters_ReturnsAllItems() {
        when(inventoryRepository.findAll()).thenReturn(testInventory);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));

        PaginatedResponse<SearchResultItem> response = searchService.searchItems(
                null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(3, response.getTotalResults());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());

        List<SearchResultItem> results = response.getResults();
        assertEquals(3, results.size());

        assertEquals("Apple", results.get(0).getBrand());
        assertEquals("Orange", results.get(1).getBrand());
        assertEquals("Dairy Farm", results.get(2).getBrand());

        verify(inventoryRepository).findAll();
    }

    @Test
    void testSearchItems_ByBrand_ReturnsFilteredItems() {
        when(inventoryRepository.findAll()).thenReturn(testInventory);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));
        List<String> brands = Collections.singletonList("Apple");

        PaginatedResponse<SearchResultItem> response = searchService.searchItems(
                brands, null, null, null, pageable);
        assertNotNull(response);

        assertEquals(1, response.getTotalResults());
        assertEquals(1, response.getTotalPages());

        List<SearchResultItem> results = response.getResults();
        assertEquals(1, results.size());
        assertEquals("Apple", results.get(0).getBrand());
        assertEquals("Fruit", results.get(0).getCategory());
        assertEquals(2.99, results.get(0).getPrice());
        assertEquals(100, results.get(0).getQuantity());

        verify(inventoryRepository).findAll();
    }

    @Test
    void testSearchItems_ByPriceRange_ReturnsFilteredItems() {
        when(inventoryRepository.findAll()).thenReturn(testInventory);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"));
        Double minPrice = 3.0;
        Double maxPrice = 4.0;

        PaginatedResponse<SearchResultItem> response = searchService.searchItems(
                null, null, minPrice, maxPrice, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals(1, response.getTotalPages());

        List<SearchResultItem> results = response.getResults();
        assertEquals(1, results.size());
        assertEquals("Orange", results.get(0).getBrand());
        assertEquals(3.49, results.get(0).getPrice());

        verify(inventoryRepository).findAll();
    }

    @Test
    void testSearchItems_WithSorting_ReturnsSortedItems() {

        when(inventoryRepository.findAll()).thenReturn(testInventory);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "quantity"));


        PaginatedResponse<SearchResultItem> response = searchService.searchItems(
                null, null, null, null, pageable);

        assertNotNull(response);
        assertEquals(3, response.getTotalResults());

        List<SearchResultItem> results = response.getResults();
        assertEquals(3, results.size());

        assertEquals(100, results.get(0).getQuantity()); // Apple
        assertEquals(75, results.get(1).getQuantity());  // Orange
        assertEquals(50, results.get(2).getQuantity());  // Milk

        verify(inventoryRepository).findAll();
    }

    @Test
    void testSearchItems_WithAllFiltersAndSorting_ReturnsFilteredAndSortedItems() {
        // Arrange
        when(inventoryRepository.findAll()).thenReturn(testInventory);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "price"));
        List<String> brands = Arrays.asList("Apple", "Orange");
        List<String> categories = Collections.singletonList("Fruit");
        Double minPrice = 2.0;
        Double maxPrice = 4.0;

        PaginatedResponse<SearchResultItem> response = searchService.searchItems(
                brands, categories, minPrice, maxPrice, pageable);

        assertNotNull(response);
        assertEquals(2, response.getTotalResults());
        assertEquals(1, response.getTotalPages());

        List<SearchResultItem> results = response.getResults();
        assertEquals(2, results.size());

        // Should be sorted by price in descending order (Orange then Apple)
        assertEquals("Orange", results.get(0).getBrand());
        assertEquals(3.49, results.get(0).getPrice());

        assertEquals("Apple", results.get(1).getBrand());
        assertEquals(2.99, results.get(1).getPrice());

        verify(inventoryRepository).findAll();
    }

}