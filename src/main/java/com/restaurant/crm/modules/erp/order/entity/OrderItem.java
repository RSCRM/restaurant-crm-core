package com.restaurant.crm.modules.erp.order.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrderConstants.TABLE_ORDER_ITEM)
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
}
