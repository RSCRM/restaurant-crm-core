package com.restaurant.crm.modules.erp.order.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerOrderStageTest {

    @Test
    void mapsEveryOrderItemStatusToTheRightStageAndOrder() {
        assertStage(OrderItemStatus.PENDING, CustomerOrderStage.RECEIVED, 1);
        assertStage(OrderItemStatus.IN_PROGRESS, CustomerOrderStage.COOKING, 2);
        assertStage(OrderItemStatus.READY_TO_SERVE, CustomerOrderStage.READY_TO_SERVE, 3);
        assertStage(OrderItemStatus.SERVED, CustomerOrderStage.SERVED, 4);
        assertStage(OrderItemStatus.CANCELLED, CustomerOrderStage.CANCELLED, 0);
    }

    @Test
    void everyOrderItemStatusIsMapped() {
        // Guards against a new OrderItemStatus being added without updating the customer mapping.
        for (OrderItemStatus status : OrderItemStatus.values()) {
            CustomerOrderStage.from(status); // must not throw
        }
    }

    @Test
    void orderItemStatusHasExactlyFiveValues() {
        // If this fails, someone changed the state machine — revisit CustomerOrderStage.from(...).
        assertEquals(5, OrderItemStatus.values().length);
    }

    private void assertStage(OrderItemStatus status, CustomerOrderStage expected, int expectedOrder) {
        CustomerOrderStage stage = CustomerOrderStage.from(status);
        assertEquals(expected, stage);
        assertEquals(expectedOrder, stage.getStageOrder());
    }
}
