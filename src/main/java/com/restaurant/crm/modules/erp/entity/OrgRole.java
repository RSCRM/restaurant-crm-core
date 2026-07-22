package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.org_role.OrgRoleConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrgRoleConstants.TABLE_ORG_ROLE,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_org_roles_org_name",
                        columnNames = {
                                OrgRoleConstants.COL_ORGANIZATION_ID,
                                OrgRoleConstants.COL_ROLE_NAME
                        })
        })
public class OrgRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = OrgRoleConstants.COL_ORGANIZATION_ID, nullable = false)
    Organization organization;

    @Column(name = OrgRoleConstants.COL_ROLE_NAME,
            nullable = false,
            columnDefinition = OrgRoleConstants.ROLE_NAME_DEFINITION)
    @Size(min = OrgRoleConstants.MIN_CHARS_ROLE_NAME,
            max = OrgRoleConstants.MAX_CHARS_ROLE_NAME)
    String roleName;

    @Column(name = OrgRoleConstants.COL_DESCRIPTION,
            columnDefinition = OrgRoleConstants.DESCRIPTION_DEFINITION)
    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = OrgRoleConstants.TABLE_ORG_ROLE_PERMISSION,
            joinColumns = @JoinColumn(name = OrgRoleConstants.COL_ROLE_ID),
            inverseJoinColumns = @JoinColumn(name = OrgRoleConstants.COL_PERMISSION_ID))
    Set<OrgPermission> permissions;
}
