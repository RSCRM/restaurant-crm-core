package com.restaurant.crm.modules.crm.loyalty_voucher.dto.response;

import com.restaurant.crm.modules.crm.loyalty_voucher.enums.CustomerVoucherStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerVoucherResponse {
    String id;
    String customerId;
    String restaurantId;
    VoucherResponse voucher;
    String voucherSn;
    CustomerVoucherStatus status;
    Instant usedAt;
    String orderId;
    Instant createdAt;
}
