package com.restaurant.crm.modules.erp.order.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = OrderConstants.TABLE_ORDER_ITEM,
        indexes = {
                // Kitchen board query: filter by status, then sort by priority and age.
                // branch_id is not duplicated here; it is reached via orders, whose
                // uk_orders_branch_order_code already indexes branch_id.
                @Index(
                        name = OrderConstants.IDX_ORDER_ITEMS_KITCHEN_BOARD,
                        columnList = OrderConstants.COL_STATUS + ", "
                                + OrderConstants.COL_PRIORITY_FLAG + ", "
                                + OrderConstants.COL_CREATED_AT
                )
        }
)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrderConstants.COL_ORDER_ID, nullable = false)
    Order order;

    @Column(name = OrderConstants.COL_PRODUCT_ID, columnDefinition = OrderConstants.UUID_DEFINITION)
    String productId;

    @Column(name = OrderConstants.COL_COMBO_ID, columnDefinition = OrderConstants.UUID_DEFINITION)
    String comboId;

    @Column(name = OrderConstants.COL_QUANTITY, nullable = false)
    Integer quantity;

    @Builder.Default
    @Column(
            name = OrderConstants.COL_UNIT_PRICE,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal unitPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(
            name = OrderConstants.COL_SUBTOTAL,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = OrderConstants.COL_NOTE, columnDefinition = OrderConstants.NOTE_DEFINITION)
    String note;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = OrderConstants.COL_STATUS, nullable = false, columnDefinition = OrderConstants.ENUM_DEFINITION)
    OrderItemStatus status = OrderItemStatus.PENDING;

    // --- Kitchen status-lifecycle columns (uc-scf-01..06) ---

    /** References users.id (not employees.id), consistent with BaseEntity#createdBy. */
    @Column(name = OrderConstants.COL_PREPARED_BY, columnDefinition = OrderConstants.UUID_DEFINITION)
    String preparedBy;

    @Column(name = OrderConstants.COL_STARTED_AT)
    Instant startedAt;

    @Column(name = OrderConstants.COL_COMPLETED_AT)
    Instant completedAt;

    @Column(name = OrderConstants.COL_CANCEL_REASON, columnDefinition = OrderConstants.NOTE_DEFINITION)
    String cancelReason;

    @Builder.Default
    @Column(name = OrderConstants.COL_PRIORITY_FLAG, nullable = false)
    boolean priorityFlag = false;
}
