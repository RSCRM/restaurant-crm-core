package com.restaurant.crm.modules.erp.contract.dto.response;

import com.restaurant.crm.modules.erp.shared.enums.LicenseStatus;
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
public class LicenseInfoResponse {
    String licenseId;
    String licenseKey;
    LicenseStatus status;
    LocalDate startedAt;
    LocalDate expiredAt;
    LocalDate revokedAt;
    String organizationId;
    String organizationName;
    String licensePlanId;
    String licensePlanName;
    BigDecimal price;
    Integer durationDays;
    Integer maxBranches;
    long usedBranches;
}
