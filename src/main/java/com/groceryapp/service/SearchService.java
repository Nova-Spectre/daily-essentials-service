package com.groceryapp.service;

import java.util.List;
import com.groceryapp.dto.response.common.PaginatedResponse;
import com.groceryapp.dto.response.search.SearchResultItem;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SearchService {
    PaginatedResponse<SearchResultItem> searchItems(List<String> brands, List<String> categories, Double minPrice,
            Double maxPrice, Pageable pageable);
}
