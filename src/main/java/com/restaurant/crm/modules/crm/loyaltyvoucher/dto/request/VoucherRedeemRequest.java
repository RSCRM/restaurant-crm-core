package com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherRedeemRequest {

    @NotBlank(message = "CUSTOMER_NOT_FOUND") // We can use CUSTOMER_NOT_FOUND or CUSTOMER_PHONE_REQUIRED etc., but let's use CUSTOMER_NOT_FOUND
    String customerId;

    @NotBlank(message = "BRANCH_ID_REQUIRED")
    String branchId;

    @NotBlank(message = "VOUCHER_NOT_FOUND")
    String voucherId;
}
