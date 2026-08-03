package com.restaurant.crm.modules.erp.inventory.enums;

public enum InventoryTransactionType {
    PURCHASE,     // Import ingredient from supplier
    SALE,         // Ingredient used for orders
    ADJUSTMENT,   // Manual stock correction
    WASTE,        // Spoiled/damaged ingredient
    RETURN        // Returned ingredient
}
