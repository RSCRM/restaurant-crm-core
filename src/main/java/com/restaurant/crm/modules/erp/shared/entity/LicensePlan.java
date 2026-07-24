package com.restaurant.crm.modules.erp.shared.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.shared.constants.license.LicensePlanConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = LicensePlanConstants.TABLE_LICENSE_PLAN)
public class LicensePlan extends BaseEntity {

    @Column(name = LicensePlanConstants.COL_PLAN_NAME,
            nullable = false,
            unique = true,
            columnDefinition = LicensePlanConstants.PLAN_NAME_DEFINITION)
    @Size(min = LicensePlanConstants.MIN_CHARS_PLAN_NAME,
            max = LicensePlanConstants.MAX_CHARS_PLAN_NAME)
    String planName;

    @Column(name = LicensePlanConstants.COL_DESCRIPTION,
            columnDefinition = LicensePlanConstants.DESCRIPTION_DEFINITION)
    String description;

    @Column(name = LicensePlanConstants.COL_PRICE, nullable = false)
    BigDecimal price;

    @Column(name = LicensePlanConstants.COL_MAX_BRANCHES, nullable = false)
    Integer maxBranches;

    @Column(name = LicensePlanConstants.COL_DURATION_DAYS, nullable = false)
    Integer durationDays;

    @Builder.Default
    @Column(name = LicensePlanConstants.COL_ENABLED, nullable = false)
    boolean enabled = true;
}
