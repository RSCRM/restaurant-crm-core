package com.restaurant.crm.modules.erp.order.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Transition-table coverage for uc-scf-01..06.
 * Source of truth: docs/kds/order-item-state-machine.md section 2.
 */
class OrderItemStatusTest {

    /** The only transitions the doc allows. Everything else must be rejected. */
    private static final Map<OrderItemStatus, Set<OrderItemStatus>> ALLOWED = Map.of(
            OrderItemStatus.PENDING, EnumSet.of(OrderItemStatus.IN_PROGRESS, OrderItemStatus.CANCELLED),
            OrderItemStatus.IN_PROGRESS, EnumSet.of(OrderItemStatus.READY_TO_SERVE, OrderItemStatus.CANCELLED),
            OrderItemStatus.READY_TO_SERVE, EnumSet.of(OrderItemStatus.SERVED),
            OrderItemStatus.SERVED, EnumSet.noneOf(OrderItemStatus.class),
            OrderItemStatus.CANCELLED, EnumSet.noneOf(OrderItemStatus.class)
    );

    @ParameterizedTest
    @CsvSource({
            "PENDING, IN_PROGRESS",
            "PENDING, CANCELLED",
            "IN_PROGRESS, READY_TO_SERVE",
            "IN_PROGRESS, CANCELLED",
            "READY_TO_SERVE, SERVED"
    })
    void allowsTransitionsInTheTable(OrderItemStatus from, OrderItemStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            // skipping ahead
            "PENDING, READY_TO_SERVE",
            "PENDING, SERVED",
            "IN_PROGRESS, SERVED",
            // going backwards
            "IN_PROGRESS, PENDING",
            "READY_TO_SERVE, IN_PROGRESS",
            // leaving a terminal state
            "SERVED, PENDING",
            "CANCELLED, IN_PROGRESS",
            // explicitly forbidden (open question #1 in the spec)
            "READY_TO_SERVE, CANCELLED"
    })
    void rejectsTransitionsNotInTheTable(OrderItemStatus from, OrderItemStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    void rejectsNullTarget() {
        assertThat(OrderItemStatus.PENDING.canTransitionTo(null)).isFalse();
    }

    @Test
    void rejectsTransitionToSelf() {
        for (OrderItemStatus status : OrderItemStatus.values()) {
            assertThat(status.canTransitionTo(status)).isFalse();
        }
    }

    /** Exhaustive cross-check of every (from, to) pair against the table above. */
    @Test
    void matchesTheFullTransitionMatrix() {
        for (OrderItemStatus from : OrderItemStatus.values()) {
            for (OrderItemStatus to : OrderItemStatus.values()) {
                boolean expected = ALLOWED.get(from).contains(to);
                assertThat(from.canTransitionTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(expected);
            }
        }
    }
}
