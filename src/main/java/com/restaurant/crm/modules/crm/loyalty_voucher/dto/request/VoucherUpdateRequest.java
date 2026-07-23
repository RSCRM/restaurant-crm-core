package com.restaurant.crm.modules.crm.loyalty_voucher.dto.request;

import com.restaurant.crm.modules.crm.loyalty_voucher.constants.VoucherConstants;
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

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherUpdateRequest {

    @NotBlank(message = "VOUCHER_TITLE_REQUIRED")
    String title;

    @NotNull(message = "VOUCHER_DISCOUNT_REQUIRED")
    @Min(value = VoucherConstants.MIN_DISCOUNT_PERCENT, message = "VOUCHER_DISCOUNT_INVALID")
    @Max(value = VoucherConstants.MAX_DISCOUNT_PERCENT, message = "VOUCHER_DISCOUNT_INVALID")
    Integer discountPercent;

    @NotNull(message = "VOUCHER_MIN_BILL_REQUIRED")
    @Min(value = 0, message = "VOUCHER_MIN_BILL_INVALID")
    BigDecimal minBillAmount;

    @NotNull(message = "VOUCHER_POINTS_REQUIRED")
    @Min(value = 0, message = "VOUCHER_POINTS_INVALID")
    Integer pointsRequired;

    @NotNull(message = "VOUCHER_ACTIVE_REQUIRED")
    Short isActive;
}
