package com.restaurant.crm.modules.erp.order.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enum representing the status of an item within an order in the kitchen workflow.
 */
public enum OrderItemStatus {
    PENDING,        // Item is waiting to be processed by the kitchen
    IN_PROGRESS,    // Item is currently being prepared/cooked by the kitchen staff
    READY_TO_SERVE, // Item has been cooked/prepared and is ready to be delivered to the table
    SERVED,         // Item has been served to the customer
    CANCELLED;      // Item has been cancelled and will not be prepared

    /** The only transitions the kitchen workflow allows; everything else is rejected. */
    private static final Map<OrderItemStatus, Set<OrderItemStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, EnumSet.of(READY_TO_SERVE, CANCELLED),
            READY_TO_SERVE, EnumSet.of(SERVED),
            SERVED, EnumSet.noneOf(OrderItemStatus.class),
            CANCELLED, EnumSet.noneOf(OrderItemStatus.class)
    );

    /**
     * Whether this status may move to {@code target}.
     * Transition table: docs/kds/order-item-state-machine.md (section 2).
     */
    public boolean canTransitionTo(OrderItemStatus target) {
        return target != null && ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}
