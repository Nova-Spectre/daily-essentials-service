package com.groceryapp.model;

import com.groceryapp.constant.inventory.InventoryStatus;
import com.groceryapp.model.common.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Inventory extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    public void updateStatus() {
        this.status = (quantity > 0) ? InventoryStatus.AVAILABLE : InventoryStatus.OUT_OF_STOCK;
    }
}