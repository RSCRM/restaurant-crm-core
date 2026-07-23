package com.restaurant.crm.modules.erp.shared.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.shared.constants.organization_branch.OrganizationBranchConstants;
import com.restaurant.crm.modules.erp.shared.enums.OrganizationBranchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrganizationBranchConstants.TABLE_ORGANIZATION_BRANCH,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_branches_org_name",
                        columnNames = {
                                OrganizationBranchConstants.COL_ORGANIZATION_ID,
                                OrganizationBranchConstants.COL_BRANCH_NAME
                        })
        })
public class OrganizationBranch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrganizationBranchConstants.COL_ORGANIZATION_ID, nullable = false)
    Organization organization;

    @Column(name = OrganizationBranchConstants.COL_MANAGER_ID, unique = true)
    String managerId;

    @NotBlank
    @NotNull
    @Column(name = OrganizationBranchConstants.COL_BRANCH_NAME,
            nullable = false,
            columnDefinition = OrganizationBranchConstants.BRANCH_NAME_DEFINITION)
    @Size(max = OrganizationBranchConstants.MAX_CHARS_BRANCH_NAME)
    String branchName;

    @Column(name = OrganizationBranchConstants.COL_ADDRESS,
            columnDefinition = OrganizationBranchConstants.ADDRESS_DEFINITION)
    String address;

    @Column(name = OrganizationBranchConstants.COL_PHONE,
            columnDefinition = OrganizationBranchConstants.PHONE_DEFINITION)
    String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = OrganizationBranchConstants.COL_STATUS, nullable = false)
    OrganizationBranchStatus status = OrganizationBranchStatus.ACTIVE;
}
