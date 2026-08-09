package com.restaurant.crm.modules.erp.inventory.enums;

public enum InventoryTransactionType {
    PURCHASE,
    SALE,
    ADJUSTMENT,
    WASTE,
    RETURN;

    public boolean isValidDirection(InventoryTransactionDirection direction) {
        return switch (this) {
            case PURCHASE, RETURN -> direction == InventoryTransactionDirection.IN;
            case SALE, WASTE -> direction == InventoryTransactionDirection.OUT;
            case ADJUSTMENT -> true; // allow both IN and OUT
        };
    }
}
