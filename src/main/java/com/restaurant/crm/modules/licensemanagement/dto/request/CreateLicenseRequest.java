package com.restaurant.crm.modules.licensemanagement.dto.request;

import com.restaurant.crm.modules.licensemanagement.constants.license.LicenseConstants;
import com.restaurant.crm.modules.licensemanagement.enums.BillingCycle;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateLicenseRequest {

    @NotBlank(message = "LICENSE_CODE_REQUIRED")
    @Size(max = LicenseConstants.MAX_CODE)
    String code;

    @NotBlank(message = "LICENSE_NAME_REQUIRED")
    @Size(max = LicenseConstants.MAX_NAME)
    String name;

    @Size(max = LicenseConstants.MAX_DESCRIPTION)
    String description;

    @NotNull(message = "LICENSE_PRICE_REQUIRED")
    @DecimalMin(value = "0", inclusive = true, message = "LICENSE_PRICE_INVALID")
    BigDecimal price;

    @NotNull(message = "LICENSE_BILLING_CYCLE_REQUIRED")
    BillingCycle billingCycle;

    @NotNull(message = "LICENSE_MAX_BRANCH_REQUIRED")
    @Min(value = LicenseConstants.MIN_MAX_BRANCH, message = "LICENSE_MAX_BRANCH_INVALID")
    Integer maxBranch;

    @NotNull(message = "LICENSE_MAX_EMPLOYEE_REQUIRED")
    @Min(value = LicenseConstants.MIN_MAX_EMPLOYEE, message = "LICENSE_MAX_EMPLOYEE_INVALID")
    Integer maxEmployee;
}
