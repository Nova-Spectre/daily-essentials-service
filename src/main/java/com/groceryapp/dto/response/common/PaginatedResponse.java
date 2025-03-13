package com.groceryapp.dto.response.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> results;
    private Integer totalResults;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;

    /**
     * Factory method to create a paginated response
     */
    public static <T> PaginatedResponse<T> of(List<T> results, Integer totalResults, Integer totalPages,
            Integer currentPage, Integer pageSize) {

        return PaginatedResponse.<T> builder().results(results).totalResults(totalResults).totalPages(totalPages)
                .currentPage(currentPage).pageSize(pageSize).build();
    }
}