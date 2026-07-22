package com.restaurant.crm.modules.erp.organization.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.identity.constants.role.RoleConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
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
@Table(name = OrgRoleConstants.TABLE_ROLE)
public class OrgRole extends BaseEntity {

        @Column(name = OrgRoleConstants.COL_ROLE_NAME,
                nullable = false, unique = true,
                columnDefinition = OrgRoleConstants.ROLE_NAME_DEFINITION)
        @Size(min = OrgRoleConstants.MIN_CHARS_ROLE_NAME,
                max = OrgRoleConstants.MAX_CHARS_ROLE_NAME)
        String roleName;

        @ManyToMany(fetch = FetchType.LAZY)
        Set<OrgPermission> orgPermissions;
}