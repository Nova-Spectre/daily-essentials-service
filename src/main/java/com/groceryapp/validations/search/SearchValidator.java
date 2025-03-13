package com.groceryapp.validations.search;

import com.groceryapp.constant.common.Constants;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchValidator {
    @Getter
    private final List<String> errors = new ArrayList<>();

    public boolean validate(List<String> brands, List<String> categories, Double minPrice, Double maxPrice,
            String sortBy, String sortDirection, int page, int size) {
        errors.clear();

        validatePriceRange(minPrice, maxPrice);
        validateSortField(sortBy);
        validateSortDirection(sortDirection);
        validatePagination(page, size);
        validateListParams(brands, categories);

        return errors.isEmpty();
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < Constants.MIN_ALLOWED_PRICE) {
            errors.add("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice < Constants.MIN_ALLOWED_PRICE) {
            errors.add("Maximum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            errors.add("Minimum price cannot be greater than maximum price");
        }
    }

    private void validateSortField(String sortBy) {
        if (sortBy != null && !Constants.VALID_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            errors.add("Invalid sort field." + " Valid fields are: " + Constants.VALID_SORT_FIELDS);
        }
    }

    private void validateSortDirection(String sortDirection) {
        if (sortDirection != null && !Constants.VALID_SORT_DIRECTIONS.contains(sortDirection.toLowerCase())) {
            errors.add("Invalid sort direction. " + " Valid directions are: " + Constants.VALID_SORT_DIRECTIONS);
        }
    }

    private void validatePagination(int page, int size) {
        if (page < Constants.MIN_PAGE) {
            errors.add("Page number cannot be negative");
        }

        if (size < Constants.MIN_SIZE) {
            errors.add("Page size must be at least " + Constants.MIN_SIZE);
        }

        if (size > Constants.MAX_SIZE) {
            errors.add("Page size cannot exceed " + Constants.MAX_SIZE);
        }
    }

    private void validateListParams(List<String> brands, List<String> categories) {
        if (!CollectionUtils.isEmpty(brands)) {
            validateMaxListSize(brands, "brands");
        }

        if (!CollectionUtils.isEmpty(categories)) {
            validateMaxListSize(categories, "categories");
        }
    }

    private void validateMaxListSize(List<?> list, String paramName) {
        if (list.size() > Constants.MAX_LIST_SIZE) {
            errors.add(paramName + " list cannot contain more than " + Constants.MAX_LIST_SIZE + " items");
        }
    }

    public String normalizeSortField(String sortBy) {
        if (sortBy == null) {
            return "price";
        }

        if ("itemqty".equalsIgnoreCase(sortBy)) {
            return "quantity";
        }

        return Constants.VALID_SORT_FIELDS.contains(sortBy.toLowerCase()) ? sortBy.toLowerCase() : "price";
    }
}
