package com.restaurant.crm.modules.erp.order.enums;

/**
 * Enum representing the status of an item within an order in the kitchen workflow.
 */
public enum OrderItemStatus {
    PENDING,        // Item is waiting to be processed by the kitchen
    IN_PROGRESS,    // Item is currently being prepared/cooked by the kitchen staff
    READY_TO_SERVE, // Item has been cooked/prepared and is ready to be delivered to the table
    SERVED,         // Item has been served to the customer
    CANCELLED       // Item has been cancelled and will not be prepared
}
