package com.restaurant.crm.modules.licensemanagement.dto.response;

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
public class LicenseResponse {
    String id;
    String code;
    String name;
    String description;
    BigDecimal price;
    BillingCycle billingCycle;
    Integer maxBranch;
    Integer maxEmployee;
    LicenseStatus status;
    Instant createdAt;
    Instant updatedAt;
}
