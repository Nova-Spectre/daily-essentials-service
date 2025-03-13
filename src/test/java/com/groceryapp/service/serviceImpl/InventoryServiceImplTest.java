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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Brand brand;
    private Category category;
    private Item item;
    private Inventory inventory;
    private InventoryRequest request;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("TestBrand");

        category = new Category();
        category.setId(1L);
        category.setName("TestCategory");

        item = new Item();
        item.setId(1L);
        item.setBrand(brand);
        item.setCategory(category);
        item.setPrice(10.0);

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setItem(item);
        inventory.setQuantity(5);
        inventory.setStatus(InventoryStatus.AVAILABLE);

        request = new InventoryRequest();
        request.setBrand("TestBrand");
        request.setCategory("TestCategory");
        request.setPrice(10.0);
        request.setQuantity(5);
    }

    @Test
    void testAddInventory_NewItem() {
        when(brandRepository.findByName("TestBrand")).thenReturn(Optional.of(brand));
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.of(category));
        when(itemRepository.findByCategoryAndBrand(category, brand)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(inventoryRepository.findByItem(item)).thenReturn(Optional.empty());

        Inventory savedInventory = new Inventory();
        savedInventory.setItem(item);
        savedInventory.setQuantity(5);
        savedInventory.setStatus(InventoryStatus.AVAILABLE);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

        InventoryResponse response = inventoryService.addInventory(request);

        assertNotNull(response);
        assertEquals("TestBrand", response.getBrand());
        assertEquals("TestCategory", response.getCategory());
        assertEquals(5, response.getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, response.getStatus());

        verify(brandRepository).findByName("TestBrand");
        verify(categoryRepository).findByName("TestCategory");
        verify(itemRepository).findByCategoryAndBrand(category, brand);
        verify(itemRepository).save(any(Item.class));
        verify(inventoryRepository).findByItem(any(Item.class));
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void testAddInventory_UpdateExistingInventory() {
        when(brandRepository.findByName("TestBrand")).thenReturn(Optional.of(brand));
        when(categoryRepository.findByName("TestCategory")).thenReturn(Optional.of(category));
        when(itemRepository.findByCategoryAndBrand(category, brand)).thenReturn(Optional.of(item));

        Inventory existingInventory = new Inventory();
        existingInventory.setItem(item);
        existingInventory.setQuantity(10);
        existingInventory.setStatus(InventoryStatus.AVAILABLE);
        when(inventoryRepository.findByItem(item)).thenReturn(Optional.of(existingInventory));

        Inventory updatedInventory = new Inventory();
        updatedInventory.setItem(item);
        updatedInventory.setQuantity(15);
        updatedInventory.setStatus(InventoryStatus.AVAILABLE);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);

        InventoryResponse response = inventoryService.addInventory(request);

        assertNotNull(response);
        assertEquals("TestBrand", response.getBrand());
        assertEquals("TestCategory", response.getCategory());
        assertEquals(15, response.getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, response.getStatus());

        verify(brandRepository).findByName("TestBrand");
        verify(categoryRepository).findByName("TestCategory");
        verify(itemRepository).findByCategoryAndBrand(category, brand);
        verify(inventoryRepository).findByItem(item);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void testGetAllInventory() throws Exception {
        // Arrange
        Inventory inventory1 = new Inventory();
        inventory1.setItem(item);
        inventory1.setQuantity(5);
        inventory1.setStatus(InventoryStatus.AVAILABLE);

        Item item2 = new Item();
        item2.setBrand(brand);
        item2.setCategory(category);
        item2.setPrice(20.0);

        Inventory inventory2 = new Inventory();
        inventory2.setItem(item2);
        inventory2.setQuantity(0);
        inventory2.setStatus(InventoryStatus.OUT_OF_STOCK);
        when(inventoryRepository.findAll()).thenReturn(Arrays.asList(inventory1, inventory2));


        List<InventoryResponse> responses = inventoryService.getAllInventory().get();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("TestBrand", responses.get(0).getBrand());
        assertEquals("TestCategory", responses.get(0).getCategory());
        assertEquals(5, responses.get(0).getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, responses.get(0).getStatus());

        assertEquals("TestBrand", responses.get(1).getBrand());
        assertEquals("TestCategory", responses.get(1).getCategory());
        assertEquals(0, responses.get(1).getQuantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, responses.get(1).getStatus());

        verify(inventoryRepository).findAll();
    }

    @Test
    void testAddInventory_InvalidQuantity() {
        request.setQuantity(0);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            inventoryService.addInventory(request);
        });

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Quantity to add must be greater than zero"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}
