package com.restaurant.crm.modules.erp.organization.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.constants.OrgPermissionConstants;
import com.restaurant.crm.modules.identity.constants.permission.PermissionConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = OrgPermissionConstants.TABLE_PERMISSION)
public class OrgPermission extends BaseEntity {
    @Column(name = OrgPermissionConstants.COL_PERMISSION_NAME,
            nullable = false,
            unique = true,
            columnDefinition = OrgPermissionConstants.PERMISSION_NAME_DEFINITION)
    @Size(min = OrgPermissionConstants.MIN_CHARS_PERMISSION_NAME,
            max = OrgPermissionConstants.MAX_CHARS_PERMISSION_NAME)
    String permissionName;
}
