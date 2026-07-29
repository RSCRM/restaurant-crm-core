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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherResponse {
    String id;
    String branchId;
    String title;
    Integer discountPercent;
    BigDecimal minBillAmount;
    Integer pointsRequired;
    Short isActive;
    Instant createdAt;
}
