package com.restaurant.crm.modules.erp.organization.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.constants.OrganizationBranchConstants;
import com.restaurant.crm.modules.erp.organization.constants.OrganizationConstants;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationStatus;
import com.restaurant.crm.modules.identity.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = OrganizationConstants.TABLE_ORGANIZATION)
public class Organization extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrganizationConstants.COL_OWNER_ID, nullable = false)
    User owner;

    @NotBlank
    @Column(name = OrganizationConstants.COL_ORGANIZATION_NAME,
            nullable = false,
            columnDefinition = OrganizationConstants.ORGANIZATION_NAME_DEFINITION)
    @Size(max = OrganizationConstants.MAX_CHARS_ORGANIZATION_NAME)
    String organizationName;

    @Column(name = OrganizationConstants.COL_TAX_CODE,
            columnDefinition = OrganizationConstants.TAX_CODE_DEFINITION)
    @Size(max = OrganizationConstants.MAX_CHARS_TAX_CODE)
    String taxCode;

    @Column(name = OrganizationConstants.COL_ADDRESS,
            columnDefinition = OrganizationConstants.ADDRESS_DEFINITION)
    @Size(max = OrganizationConstants.MAX_CHARS_ADDRESS)
    String address;

    @Column(name = OrganizationConstants.COL_PHONE,
            columnDefinition = OrganizationConstants.PHONE_DEFINITION)
    @Size(max = OrganizationConstants.MAX_CHARS_PHONE)
    String phone;

    @Column(name = OrganizationConstants.COL_EMAIL,
            columnDefinition = OrganizationConstants.EMAIL_DEFINITION)
    @Email
    @Size(max = OrganizationConstants.MAX_CHARS_EMAIL)
    String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = OrganizationConstants.COL_STATUS,
            nullable = false
    )
    OrganizationStatus status = OrganizationStatus.ACTIVE;
}
