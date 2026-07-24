package com.restaurant.crm.modules.erp.shared.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.shared.constants.license.LicenseContractConstants;
import com.restaurant.crm.modules.erp.shared.enums.LicenseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = LicenseContractConstants.TABLE_LICENSE_CONTRACT)
public class LicenseContract extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = LicenseContractConstants.COL_ORGANIZATION_ID, nullable = false)
    Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = LicenseContractConstants.COL_LICENSE_PLAN_ID, nullable = false)
    LicensePlan licensePlan;

    @Column(name = LicenseContractConstants.COL_LICENSE_KEY,
            nullable = false,
            unique = true,
            columnDefinition = LicenseContractConstants.LICENSE_KEY_DEFINITION)
    @Size(max = 120)
    String licenseKey;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = LicenseContractConstants.COL_STATUS, nullable = false)
    LicenseStatus status = LicenseStatus.ACTIVE;

    @Column(name = LicenseContractConstants.COL_STARTED_AT, nullable = false)
    LocalDate startedAt;

    @Column(name = LicenseContractConstants.COL_EXPIRED_AT, nullable = false)
    LocalDate expiredAt;

    @Column(name = LicenseContractConstants.COL_REVOKED_AT)
    LocalDate revokedAt;

    @Column(name = LicenseContractConstants.COL_NOTE,
            columnDefinition = LicenseContractConstants.NOTE_DEFINITION)
    String note;
}
