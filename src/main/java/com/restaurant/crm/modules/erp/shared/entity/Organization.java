package com.restaurant.crm.modules.erp.shared.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.shared.constants.organization.OrganizationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.restaurant.crm.modules.erp.shared.enums.OrganizationStatus;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrganizationConstants.TABLE_ORGANIZATION,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organizations_owner_name",
                        columnNames = {
                                OrganizationConstants.COL_OWNER_ID,
                                OrganizationConstants.COL_ORGANIZATION_NAME
                        })
        })
public class Organization extends BaseEntity {

    @NotBlank
    @NotNull
    @Column(name = OrganizationConstants.COL_OWNER_ID, nullable = false)
    String ownerId;

    @NotBlank
    @NotNull
    @Column(name = OrganizationConstants.COL_ORGANIZATION_NAME,
            nullable = false,
            columnDefinition = OrganizationConstants.ORGANIZATION_NAME_DEFINITION)
    @Size(max = OrganizationConstants.MAX_CHARS_ORGANIZATION_NAME)
    String organizationName;

    @Column(name = OrganizationConstants.COL_TAX_CODE,
            columnDefinition = OrganizationConstants.TAX_CODE_DEFINITION)
    String taxCode;

    @Column(name = OrganizationConstants.COL_ADDRESS,
            columnDefinition = OrganizationConstants.ADDRESS_DEFINITION)
    String address;

    @Column(name = OrganizationConstants.COL_PHONE,
            columnDefinition = OrganizationConstants.PHONE_DEFINITION)
    String phone;

    @Email
    @Column(name = OrganizationConstants.COL_EMAIL,
            columnDefinition = OrganizationConstants.EMAIL_DEFINITION)
    String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = OrganizationConstants.COL_STATUS, nullable = false)
    OrganizationStatus status = OrganizationStatus.ACTIVE;
}
