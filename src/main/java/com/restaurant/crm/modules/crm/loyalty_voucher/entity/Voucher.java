package com.restaurant.crm.modules.crm.loyalty_voucher.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.loyalty_voucher.constants.VoucherConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = VoucherConstants.TABLE_VOUCHER)
public class Voucher extends BaseEntity {

    @NotNull
    @Column(name = VoucherConstants.COL_RESTAURANT_ID, nullable = false)
    String restaurantId;

    @NotBlank
    @NotNull
    @Column(name = VoucherConstants.COL_TITLE, nullable = false, columnDefinition = VoucherConstants.TITLE_DEFINITION)
    String title;

    @NotNull
    @Min(VoucherConstants.MIN_DISCOUNT_PERCENT)
    @Max(VoucherConstants.MAX_DISCOUNT_PERCENT)
    @Column(name = VoucherConstants.COL_DISCOUNT_PERCENT, nullable = false)
    Integer discountPercent;

    @Builder.Default
    @NotNull
    @Column(name = VoucherConstants.COL_MIN_BILL_AMOUNT, nullable = false, columnDefinition = VoucherConstants.MIN_BILL_AMOUNT_DEFINITION)
    BigDecimal minBillAmount = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = VoucherConstants.COL_POINTS_REQUIRED, nullable = false)
    Integer pointsRequired = 0;

    @Builder.Default
    @Column(name = VoucherConstants.COL_IS_ACTIVE, nullable = false)
    Short isActive = 1;
}
