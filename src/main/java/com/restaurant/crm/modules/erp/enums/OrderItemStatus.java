package com.restaurant.crm.modules.erp.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle status of an order item.
 * Transition table: docs/kds/order-item-state-machine.md (section 2).
 */
public enum OrderItemStatus {
    PENDING,
    IN_PROGRESS,
    READY,
    SERVED,
    CANCELLED;

    private static final Map<OrderItemStatus, Set<OrderItemStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, EnumSet.of(READY, CANCELLED),
            READY, EnumSet.of(SERVED),
            SERVED, EnumSet.noneOf(OrderItemStatus.class),
            CANCELLED, EnumSet.noneOf(OrderItemStatus.class)
    );

    public boolean canTransitionTo(OrderItemStatus target) {
        return target != null && ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}
