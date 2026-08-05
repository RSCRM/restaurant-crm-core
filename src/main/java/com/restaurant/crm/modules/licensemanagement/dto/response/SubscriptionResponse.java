package com.restaurant.crm.modules.licensemanagement.dto.response;

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
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionResponse {
    String id;
    LicenseInfo license;
    String organizationId;
    LocalDate startDate;
    LocalDate endDate;
    SubscriptionStatus status;
    BigDecimal price;
    BillingCycle billingCycle;
    Integer maxBranch;
    Integer maxEmployee;
    Instant createdAt;
    Instant updatedAt;
}
