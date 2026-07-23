package com.restaurant.crm.modules.licensemanagement.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.licensemanagement.constants.license.LicenseConstants;
import com.restaurant.crm.modules.licensemanagement.enums.BillingCycle;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
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
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = LicenseConstants.TABLE_LICENSE)
public class License extends BaseEntity {

    @Column(name = LicenseConstants.COL_CODE,
            nullable = false, unique = true,
            columnDefinition = LicenseConstants.CODE_DEFINITION)
    String code;

    @Column(name = LicenseConstants.COL_NAME,
            nullable = false,
            columnDefinition = LicenseConstants.NAME_DEFINITION)
    String name;

    @Column(name = LicenseConstants.COL_DESCRIPTION,
            columnDefinition = "TEXT")
    String description;

    @Column(name = LicenseConstants.COL_PRICE,
            nullable = false)
    BigDecimal price;

    @Column(name = LicenseConstants.COL_BILLING_CYCLE,
            nullable = false,
            columnDefinition = LicenseConstants.BILLING_CYCLE_DEFINITION)
    @Enumerated(EnumType.STRING)
    BillingCycle billingCycle;

    @Column(name = LicenseConstants.COL_MAX_BRANCH,
            nullable = false)
    Integer maxBranch;

    @Column(name = LicenseConstants.COL_MAX_EMPLOYEE,
            nullable = false)
    Integer maxEmployee;

    @Column(name = LicenseConstants.COL_STATUS,
            nullable = false,
            columnDefinition = LicenseConstants.STATUS_DEFINITION)
    @Enumerated(EnumType.STRING)
    LicenseStatus status;

    @Column(name = LicenseConstants.COL_DELETED_AT)
    Instant deletedAt;
}
