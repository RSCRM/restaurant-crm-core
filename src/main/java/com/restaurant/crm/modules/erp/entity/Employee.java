package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.employee.EmployeeConstants;
import com.restaurant.crm.modules.erp.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
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
@Table(name = EmployeeConstants.TABLE_EMPLOYEE,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employees_user_id",
                        columnNames = EmployeeConstants.COL_USER_ID)
        })
public class Employee extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_USER_ID, nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_ORG_ROLE_ID, nullable = false)
    OrgRole orgRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = EmployeeConstants.COL_BRANCH_ID, nullable = false)
    OrganizationBranch branch;

    @Column(name = EmployeeConstants.COL_FULL_NAME,
            nullable = false,
            columnDefinition = EmployeeConstants.FULL_NAME_DEFINITION)
    @Size(min = EmployeeConstants.MIN_CHARS_FULL_NAME,
            max = EmployeeConstants.MAX_CHARS_FULL_NAME)
    String fullName;

    @Email
    @Column(name = EmployeeConstants.COL_EMAIL,
            nullable = false,
            columnDefinition = EmployeeConstants.EMAIL_DEFINITION)
    String email;

    @Column(name = EmployeeConstants.COL_PHONE,
            columnDefinition = EmployeeConstants.PHONE_DEFINITION)
    String phone;

    @Builder.Default
    @Column(name = EmployeeConstants.COL_START_DATE, nullable = false)
    LocalDate startDate = LocalDate.now();

    @Column(name = EmployeeConstants.COL_END_DATE)
    LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = EmployeeConstants.COL_STATUS, nullable = false)
    EmployeeStatus status = EmployeeStatus.ACTIVE;
}
