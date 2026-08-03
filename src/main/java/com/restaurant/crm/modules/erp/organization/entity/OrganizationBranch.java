package com.restaurant.crm.modules.erp.organization.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.constants.OrganizationBranchConstants;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrganizationBranchConstants.TABLE_ORGANIZATION_BRANCH)
public class OrganizationBranch extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrganizationBranchConstants.COL_ORGANIZATION_ID, nullable = false)
    Organization organization;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrganizationBranchConstants.COL_MANAGER_ID, unique = true)
    Employee manager;

    @NotBlank
    @NotNull
    @Column(name = OrganizationBranchConstants.COL_BRANCH_NAME,
            nullable = false,
            columnDefinition = OrganizationBranchConstants.BRANCH_NAME_DEFINITION)
    @Size(max = OrganizationBranchConstants.MAX_CHARS_BRANCH_NAME)
    String branchName;

    @Column(name = OrganizationBranchConstants.COL_ADDRESS,
            columnDefinition = OrganizationBranchConstants.ADDRESS_DEFINITION)
    @Size(max = OrganizationBranchConstants.MAX_CHARS_ADDRESS)
    String address;

    @Column(name = OrganizationBranchConstants.COL_PHONE,
            columnDefinition = OrganizationBranchConstants.PHONE_DEFINITION)
    @Size(max = OrganizationBranchConstants.MAX_CHARS_PHONE)
    String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = OrganizationBranchConstants.COL_STATUS,
            nullable = false
    )
    OrganizationBranchStatus status = OrganizationBranchStatus.ACTIVE;
}
