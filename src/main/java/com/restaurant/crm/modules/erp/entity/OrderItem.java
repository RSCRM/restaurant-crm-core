package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.OrderItemConstants;
import com.restaurant.crm.modules.erp.enums.OrderItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * TEMPORARY STUB — owner: SangTD6 (uc-sw-*). Replace with the real OrderItem entity.
 *
 * <p>The base fields ({@code order}, {@code dishName}, {@code quantity}, {@code note}) are
 * placeholders so uc-scf-01 can compile and run. {@code dishName} stands in for a real menu
 * reference (HaoHN10). The status-lifecycle columns below are the real contribution of
 * DuyNHN3 (uc-scf-01..06) per docs/kds/order-item-state-machine.md section 3.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = OrderItemConstants.TABLE_ORDER_ITEM,
        indexes = {
                // Covers the kitchen board query: filter by status, sort by priority then age.
                @Index(
                        name = OrderItemConstants.IDX_KITCHEN_BOARD,
                        columnList = OrderItemConstants.COL_STATUS + ", "
                                + OrderItemConstants.COL_PRIORITY_FLAG + ", "
                                + OrderItemConstants.COL_CREATED_AT
                ),
                @Index(name = OrderItemConstants.IDX_ORDER_ID, columnList = OrderItemConstants.COL_ORDER_ID)
        }
)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrderItemConstants.COL_ORDER_ID)
    Order order;

    @Column(name = OrderItemConstants.COL_DISH_NAME, columnDefinition = OrderItemConstants.DISH_NAME_DEFINITION)
    String dishName;

    @Column(name = OrderItemConstants.COL_QUANTITY)
    Integer quantity;

    @Column(name = OrderItemConstants.COL_NOTE, columnDefinition = OrderItemConstants.NOTE_DEFINITION)
    String note;

    // --- Status-lifecycle columns owned by DuyNHN3 (uc-scf-01..06) ---

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = OrderItemConstants.COL_STATUS, nullable = false)
    OrderItemStatus status = OrderItemStatus.PENDING;

    /** References users.id (not employees.id), consistent with BaseEntity#createdBy. */
    @Column(name = OrderItemConstants.COL_PREPARED_BY, columnDefinition = OrderItemConstants.PREPARED_BY_DEFINITION)
    String preparedBy;

    @Column(name = OrderItemConstants.COL_STARTED_AT)
    Instant startedAt;

    @Column(name = OrderItemConstants.COL_COMPLETED_AT)
    Instant completedAt;

    @Column(name = OrderItemConstants.COL_CANCEL_REASON, columnDefinition = OrderItemConstants.CANCEL_REASON_DEFINITION)
    String cancelReason;

    @Builder.Default
    @Column(name = OrderItemConstants.COL_PRIORITY_FLAG, nullable = false)
    boolean priorityFlag = false;
}
