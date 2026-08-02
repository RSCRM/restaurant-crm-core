package com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVoucherResponse {
    String id;
    String customerId;
    String branchId;
    VoucherResponse voucher;
    String voucherSn;
    CustomerVoucherStatus status;
    Instant usedAt;
    String orderId;
    Instant createdAt;
}
