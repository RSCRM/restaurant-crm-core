package com.restaurant.crm.modules.erp.order.enums;

/**
 * Customer-facing cooking stage derived from {@link OrderItemStatus} (uc-c-06).
 * {@code stageOrder} lets the FE draw a progress bar without hardcoding the sequence;
 * {@code CANCELLED} is order 0 (off the progress track) but still shown.
 * The {@link #from} switch is exhaustive on purpose — adding an {@code OrderItemStatus}
 * value will fail compilation here until the mapping is updated.
 */
public enum CustomerOrderStage {

    RECEIVED(1),
    COOKING(2),
    READY_TO_SERVE(3),
    SERVED(4),
    CANCELLED(0);

    private final int stageOrder;

    CustomerOrderStage(int stageOrder) {
        this.stageOrder = stageOrder;
    }

    public int getStageOrder() {
        return stageOrder;
    }

    /** Maps a kitchen item status to the stage the customer sees (uc-c-06). */
    public static CustomerOrderStage from(OrderItemStatus status) {
        return switch (status) {
            case PENDING -> RECEIVED;
            case IN_PROGRESS -> COOKING;
            case READY_TO_SERVE -> READY_TO_SERVE;
            case SERVED -> SERVED;
            case CANCELLED -> CANCELLED;
        };
    }
}
