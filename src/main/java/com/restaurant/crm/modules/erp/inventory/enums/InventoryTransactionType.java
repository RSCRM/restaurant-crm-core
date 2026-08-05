package com.restaurant.crm.modules.erp.inventory.enums;

public enum InventoryTransactionType {
    PURCHASE,     // Import inventory from supplier
    SALE,         // Inventory used for orders
    ADJUSTMENT,   // Manual stock correction
    WASTE,        // Spoiled/damaged inventory
    RETURN        // Returned inventory
}
