package com.restaurant.crm.modules.licensemanagement.dto.request;

import com.restaurant.crm.modules.licensemanagement.enums.BillingCycle;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
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
public class LicenseSearchRequest {
    // Search
    String name;

    // Filter - Price range
    BigDecimal priceFrom;
    BigDecimal priceTo;

    // Filter - Billing cycle
    BillingCycle billingCycle;

    // Filter - Max branch range
    Integer maxBranchFrom;
    Integer maxBranchTo;

    // Filter - Max employee range
    Integer maxEmployeeFrom;
    Integer maxEmployeeTo;

    // Filter - Created date range
    Instant createdAtFrom;
    Instant createdAtTo;

    // Filter - Status
    LicenseStatus status;
}
