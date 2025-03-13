package com.groceryapp.constant.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(1000, "Invalid request parameters"), INVENTORY_ITEM_NOT_FOUND(2001, "Inventory item not found"),
    ITEM_NOT_FOUND(2002, "Item Not found"), INSUFFICIENT_INVENTORY(2003, "Insufficient Inventory");

    private final int code;
    private final String message;
}
