package com.groceryapp.service.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Override
    public PaginatedResponse<SearchResultItem> searchItems(List<String> brands, List<String> categories,
            Double minPrice, Double maxPrice, Pageable pageable) {
        List<Inventory> allInventory = inventoryRepository.findAll();
        List<Inventory> filteredInventory = applyFilters(allInventory, brands, categories, minPrice, maxPrice);

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
            if ("quantity".equals(order.getProperty())) {
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