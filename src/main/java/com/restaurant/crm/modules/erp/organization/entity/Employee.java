package com.restaurant.crm.modules.erp.organization.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeAccountConstants;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = EmployeeConstants.TABLE_EMPLOYEE)
public class Employee extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_USER_ID, nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_ORG_ROLE_ID)
    OrgRole orgRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_ORGANIZATION_ID)
    Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_BRANCH_ID)
    OrganizationBranch branch;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = EmployeeConstants.COL_STATUS, nullable = false)
    EmployeeStatus status = EmployeeAccountConstants.DEFAULT_STATUS;

    @NotNull
    @Column(name = EmployeeConstants.COL_EMAIL,
            nullable = false,
            columnDefinition = EmployeeConstants.EMAIL_DEFINITION)
    @Email
    @Size(max = EmployeeConstants.MAX_CHARS_EMAIL)
    String email;

    @Column(name = EmployeeConstants.COL_PHONE,
            columnDefinition = EmployeeConstants.PHONE_DEFINITION)
    @Size(max = EmployeeConstants.MAX_CHARS_PHONE)
    String phone;

    @NotNull
    @Column(name = EmployeeConstants.COL_START_DATE, nullable = false)
    LocalDate startDate;

    @Column(name = EmployeeConstants.COL_END_DATE)
    LocalDate endDate;

    @Column(name = EmployeeConstants.COL_SALARY, precision = 15, scale = 2)
    BigDecimal salary;

    @Builder.Default
    @Column(name = EmployeeConstants.COL_PROFILE_UPDATE_ENABLED,
            nullable = false,
            columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean profileUpdateEnabled = false;
}
