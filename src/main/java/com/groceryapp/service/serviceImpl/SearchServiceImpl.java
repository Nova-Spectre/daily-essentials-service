package com.groceryapp.service.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.groceryapp.constant.common.ErrorCode;
import com.groceryapp.exception.ServiceException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.groceryapp.constant.common.Constants;
import com.groceryapp.dto.response.common.PaginatedResponse;
import com.groceryapp.dto.response.search.SearchResultItem;
import com.groceryapp.model.Inventory;
import com.groceryapp.repository.InventoryRepository;
import com.groceryapp.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final InventoryRepository inventoryRepository;

    /**
     * Searches for items in the inventory based on provided filters (brands, categories, price range) and applies pagination.
     *
     * @param brands    List of brand names to filter by.
     * @param categories List of category names to filter by.
     * @param minPrice  Minimum price to filter items.
     * @param maxPrice  Maximum price to filter items.
     * @param pageable  Pageable object that specifies pagination details (page number, page size, sorting).
     * @return Paginated response containing the search results.
     * @throws ServiceException if no items are found in the inventory or no items match the search criteria.
     */
    @Override
    public PaginatedResponse<SearchResultItem> searchItems(List<String> brands, List<String> categories,
            Double minPrice, Double maxPrice, Pageable pageable) {
        List<Inventory> allInventory = inventoryRepository.findAll();

        if (allInventory.isEmpty()) {
            throw new ServiceException(ErrorCode.INVENTORY_ITEM_NOT_FOUND, "No items found in the inventory");
        }

        List<Inventory> filteredInventory = applyFilters(allInventory, brands, categories, minPrice, maxPrice);

        if (filteredInventory.isEmpty()) {
            throw new ServiceException(ErrorCode.ITEM_NOT_FOUND, "No items found matching the search criteria");
        }

        Sort.Order order = pageable.getSort().isSorted() ? pageable.getSort().toList().get(0) : null;
        List<Inventory> sortedInventory = applySorting(filteredInventory, order);

        List<SearchResultItem> resultItems = sortedInventory.stream()
                .skip((long) pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize())
                .map(this::mapToSearchResultItem).collect(Collectors.toList());

        int totalItems = filteredInventory.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageable.getPageSize());

        return PaginatedResponse.of(resultItems, totalItems, totalPages, pageable.getPageNumber(),
                pageable.getPageSize());
    }

    private List<Inventory> applyFilters(List<Inventory> inventory, List<String> brands, List<String> categories,
            Double minPrice, Double maxPrice) {
        Predicate<Inventory> combinedFilter = item -> true;

        if (!CollectionUtils.isEmpty(brands)) {
            combinedFilter = combinedFilter.and(item -> brands.contains(item.getItem().getBrand().getName()));
        }
        if (!CollectionUtils.isEmpty(categories)) {
            combinedFilter = combinedFilter.and(item -> categories.contains(item.getItem().getCategory().getName()));
        }
        if (minPrice != null) {
            combinedFilter = combinedFilter.and(item -> {
                Double price = item.getItem().getPrice();
                return price != null && price >= minPrice;
            });
        }
        if (maxPrice != null) {
            combinedFilter = combinedFilter.and(item -> {
                Double price = item.getItem().getPrice();
                return price != null && price <= maxPrice;
            });
        }

        return inventory.stream().filter(combinedFilter).collect(Collectors.toList());
    }

    private List<Inventory> applySorting(List<Inventory> inventory, Sort.Order order) {
        if (order == null) {
            return inventory.stream()
                    .sorted((a, b) -> Double.compare(a.getItem().getPrice() != null ? a.getItem().getPrice() : 0,
                            b.getItem().getPrice() != null ? b.getItem().getPrice() : 0))
                    .collect(Collectors.toList());
        }

        Comparator<Inventory> comparator = getInventoryComparator(order);
        return inventory.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static Comparator<Inventory> getInventoryComparator(Sort.Order order) {
        Comparator<Inventory> comparator;
        boolean isAscending = order.getDirection() == Direction.ASC;

        if (Constants.VALID_SORT_FIELDS.contains(order.getProperty())) {
            if (Constants.QUANTITY.equals(order.getProperty())) {
                comparator = Comparator.comparingInt(Inventory::getQuantity);
            } else {
                comparator = Comparator.comparing(inv -> inv.getItem().getPrice(),
                        Comparator.nullsLast(Comparator.naturalOrder()));
            }
        } else {
            comparator = Comparator.comparing(inv -> inv.getItem().getPrice(),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }

        return isAscending ? comparator : comparator.reversed();
    }

    private SearchResultItem mapToSearchResultItem(Inventory inventory) {
        return SearchResultItem.builder().brand(inventory.getItem().getBrand().getName())
                .category(inventory.getItem().getCategory().getName()).price(inventory.getItem().getPrice())
                .quantity(inventory.getQuantity()).build();
    }
}