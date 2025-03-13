package com.groceryapp.dto.request.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    private String brand;

    private String category;

    private Double price;

    private Integer quantity;
}
