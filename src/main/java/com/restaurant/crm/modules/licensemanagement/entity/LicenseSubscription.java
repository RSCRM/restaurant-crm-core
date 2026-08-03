package com.restaurant.crm.modules.licensemanagement.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.licensemanagement.constants.license_subscription.LicenseSubscriptionConstants;
import com.restaurant.crm.modules.licensemanagement.enums.BillingCycle;
import com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = LicenseSubscriptionConstants.TABLE_LICENSE_SUBSCRIPTION)
public class LicenseSubscription extends BaseEntity {

    @Column(name = LicenseSubscriptionConstants.COL_LICENSE_ID,
            nullable = false)
    String licenseId;

    @Column(name = LicenseSubscriptionConstants.COL_ORGANIZATION_ID,
            nullable = false)
    String organizationId;

    @Column(name = LicenseSubscriptionConstants.COL_START_DATE,
            nullable = false)
    LocalDate startDate;

    @Column(name = LicenseSubscriptionConstants.COL_END_DATE,
            nullable = false)
    LocalDate endDate;

    @Column(name = LicenseSubscriptionConstants.COL_STATUS,
            nullable = false,
            columnDefinition = LicenseSubscriptionConstants.STATUS_DEFINITION)
    @Enumerated(EnumType.STRING)
    SubscriptionStatus status;

    @Column(name = LicenseSubscriptionConstants.COL_PRICE,
            nullable = false)
    BigDecimal price;

    @Column(name = LicenseSubscriptionConstants.COL_BILLING_CYCLE,
            nullable = false,
            columnDefinition = LicenseSubscriptionConstants.BILLING_CYCLE_DEFINITION)
    @Enumerated(EnumType.STRING)
    BillingCycle billingCycle;

    @Column(name = LicenseSubscriptionConstants.COL_MAX_BRANCH,
            nullable = false)
    Integer maxBranch;

    @Column(name = LicenseSubscriptionConstants.COL_MAX_EMPLOYEE,
            nullable = false)
    Integer maxEmployee;
}
