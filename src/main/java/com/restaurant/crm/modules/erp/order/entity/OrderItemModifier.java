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
@Table(name = OrderConstants.TABLE_ORDER_ITEM_MODIFIER)
public class OrderItemModifier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrderConstants.COL_ORDER_ITEM_ID, nullable = false)
    OrderItem orderItem;

    @Column(name = OrderConstants.COL_MODIFIER_OPTION_ID, nullable = false, columnDefinition = OrderConstants.UUID_DEFINITION)
    String modifierOptionId;

    @Builder.Default
    @Column(
            name = OrderConstants.COL_ADDITIONAL_PRICE,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal additionalPrice = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = OrderConstants.COL_QUANTITY, nullable = false)
    Integer quantity = OrderConstants.MIN_QUANTITY;
}
