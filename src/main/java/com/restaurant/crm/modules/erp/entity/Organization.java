package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.organization.OrganizationConstants;
import com.restaurant.crm.modules.identity.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Table(name = OrganizationConstants.TABLE_ORGANIZATION,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organizations_owner_name",
                        columnNames = {
                                OrganizationConstants.COL_OWNER_ID,
                                OrganizationConstants.COL_ORGANIZATION_NAME
                        })
        })
public class Organization extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrganizationConstants.COL_OWNER_ID, nullable = false)
    User owner;

    @Column(name = OrganizationConstants.COL_ORGANIZATION_NAME,
            nullable = false,
            columnDefinition = OrganizationConstants.ORGANIZATION_NAME_DEFINITION)
    @Size(min = OrganizationConstants.MIN_CHARS_ORGANIZATION_NAME,
            max = OrganizationConstants.MAX_CHARS_ORGANIZATION_NAME)
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
}
