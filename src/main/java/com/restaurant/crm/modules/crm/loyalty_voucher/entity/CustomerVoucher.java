package com.restaurant.crm.modules.crm.loyalty_voucher.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.loyalty_voucher.constants.CustomerVoucherConstants;
import com.restaurant.crm.modules.crm.loyalty_voucher.enums.CustomerVoucherStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = CustomerVoucherConstants.TABLE_CUSTOMER_VOUCHER)
public class CustomerVoucher extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CustomerVoucherConstants.COL_CUSTOMER_ID, nullable = false)
    Customer customer;

    @NotNull
    @Column(name = CustomerVoucherConstants.COL_RESTAURANT_ID, nullable = false)
    String restaurantId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CustomerVoucherConstants.COL_VOUCHER_ID, nullable = false)
    Voucher voucher;

    @NotBlank
    @NotNull
    @Column(name = CustomerVoucherConstants.COL_VOUCHER_SN, nullable = false, unique = true, columnDefinition = CustomerVoucherConstants.VOUCHER_SN_DEFINITION)
    String voucherSn;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = CustomerVoucherConstants.COL_STATUS, nullable = false)
    CustomerVoucherStatus status = CustomerVoucherStatus.AVAILABLE;

    @Column(name = CustomerVoucherConstants.COL_USED_AT)
    Instant usedAt;

    @Column(name = CustomerVoucherConstants.COL_ORDER_ID)
    String orderId;
}
