package com.groceryapp.controller;

import java.util.List;

import com.groceryapp.constant.common.ErrorCode;
import com.groceryapp.dto.response.common.ErrorResponseWrapper;
import com.groceryapp.dto.response.common.PaginatedResponse;
import com.groceryapp.dto.response.search.SearchResultItem;
import com.groceryapp.exception.ServiceException;
import com.groceryapp.validations.search.SearchValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.groceryapp.dto.response.common.GenericResponseWrapper;
import com.groceryapp.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;
    private final SearchValidator validator;

    @GetMapping
    public ResponseEntity<GenericResponseWrapper<PaginatedResponse<SearchResultItem>>> searchItems(
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) List<String> categories, @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice, @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Starting item search with page: {}, size: {}", page, size);

        if (!validator.validate(brands, categories, minPrice, maxPrice, sortBy, sortDirection, page, size)) {
            if (!validator.validate(brands, categories, minPrice, maxPrice, sortBy, sortDirection, page, size)) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST, String.join(", ", validator.getErrors()));
            }
        }

        String normalizedSortField = validator.normalizeSortField(sortBy);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, normalizedSortField));
        PaginatedResponse<SearchResultItem> response = searchService.searchItems(brands, categories, minPrice, maxPrice,
                pageable);

        log.info("Completed item search with {} results", response.getTotalResults());
        return ResponseEntity.ok(GenericResponseWrapper.success(response));
    }

}
