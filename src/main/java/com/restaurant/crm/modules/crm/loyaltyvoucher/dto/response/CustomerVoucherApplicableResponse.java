package com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVoucherApplicableResponse {
    String customerVoucherId;
    String voucherSn;
    String title;
    Integer discountPercent;
    BigDecimal minBillAmount;
    String status;
    Instant expiredAt;
    Boolean isApplicable;
    String reason;
}
