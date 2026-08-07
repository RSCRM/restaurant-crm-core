package com.restaurant.crm.modules.erp.order.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(
        name = OrderConstants.TABLE_ORDER,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = OrderConstants.UK_BRANCH_ORDER_CODE,
                        columnNames = {OrderConstants.COL_BRANCH_ID, OrderConstants.COL_ORDER_CODE}
                )
        }
)
public class Order extends BaseEntity {

    @NotBlank
    @NotNull
    @Column(name = OrderConstants.COL_BRANCH_ID, nullable = false, columnDefinition = OrderConstants.UUID_DEFINITION)
    String branchId;

    @Column(name = OrderConstants.COL_TABLE_ID, columnDefinition = OrderConstants.UUID_DEFINITION)
    String tableId;

    @Column(name = OrderConstants.COL_RESERVATION_ID, columnDefinition = OrderConstants.UUID_DEFINITION)
    String reservationId;

    @NotBlank
    @NotNull
    @Size(max = OrderConstants.MAX_CHARS_ORDER_CODE)
    @Column(name = OrderConstants.COL_ORDER_CODE, nullable = false, columnDefinition = OrderConstants.ORDER_CODE_DEFINITION)
    String orderCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = OrderConstants.COL_ORDER_TYPE, nullable = false, columnDefinition = OrderConstants.ENUM_DEFINITION)
    OrderType orderType;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = OrderConstants.COL_STATUS, nullable = false, columnDefinition = OrderConstants.ENUM_DEFINITION)
    OrderStatus status = OrderStatus.PENDING;

    @Size(max = OrderConstants.MAX_CHARS_CUSTOMER_NAME)
    @Column(name = OrderConstants.COL_CUSTOMER_NAME, columnDefinition = OrderConstants.CUSTOMER_NAME_DEFINITION)
    String customerName;

    @Size(max = OrderConstants.MAX_CHARS_CUSTOMER_PHONE)
    @Column(name = OrderConstants.COL_CUSTOMER_PHONE, columnDefinition = OrderConstants.CUSTOMER_PHONE_DEFINITION)
    String customerPhone;

    @Size(max = OrderConstants.MAX_CHARS_NOTE)
    @Column(name = OrderConstants.COL_NOTE, columnDefinition = OrderConstants.NOTE_DEFINITION)
    String note;

    @Builder.Default
    @NotNull
    @Column(
            name = OrderConstants.COL_SUBTOTAL,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(
            name = OrderConstants.COL_DISCOUNT_AMOUNT,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(
            name = OrderConstants.COL_TOTAL_AMOUNT,
            nullable = false,
            precision = OrderConstants.MONEY_PRECISION,
            scale = OrderConstants.MONEY_SCALE
    )
    BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "applied_voucher_code")
    String appliedVoucherCode;

    @Column(name = "applied_voucher_id", columnDefinition = OrderConstants.UUID_DEFINITION)
    String appliedVoucherId;
}
