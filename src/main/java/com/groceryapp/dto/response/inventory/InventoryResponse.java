package com.groceryapp.dto.response.inventory;

import com.groceryapp.constant.inventory.InventoryStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String brand;
    private String category;
    private Integer quantity;
    private InventoryStatus status;
}
