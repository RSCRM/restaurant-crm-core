package com.restaurant.crm.modules.licensemanagement.dto.request;

import com.restaurant.crm.modules.licensemanagement.enums.BillingCycle;
import com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionSearchRequest {
    // Search fields (OR) - search on License fields
    String licenseName;
    String licenseCode;

    // Filter fields (AND) - price range
    BigDecimal priceFrom;
    BigDecimal priceTo;

    // Filter fields (AND) - maxBranch range
    Integer maxBranchFrom;
    Integer maxBranchTo;

    // Filter fields (AND) - maxEmployee range
    Integer maxEmployeeFrom;
    Integer maxEmployeeTo;

    // Filter fields (AND) - date range
    LocalDate startDateFrom;
    LocalDate startDateTo;
    LocalDate endDateFrom;
    LocalDate endDateTo;

    // Filter fields (AND) - enums
    SubscriptionStatus status;
    BillingCycle billingCycle;
}
