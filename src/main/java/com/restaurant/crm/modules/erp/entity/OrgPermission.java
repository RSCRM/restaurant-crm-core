package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.org_permission.OrgPermissionConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = OrgPermissionConstants.TABLE_ORG_PERMISSION)
public class OrgPermission extends BaseEntity {

    @Column(name = OrgPermissionConstants.COL_PERMISSION_CODE,
            nullable = false,
            unique = true,
            columnDefinition = OrgPermissionConstants.PERMISSION_CODE_DEFINITION)
    @Size(min = OrgPermissionConstants.MIN_CHARS_PERMISSION_CODE,
            max = OrgPermissionConstants.MAX_CHARS_PERMISSION_CODE)
    String permissionCode;

    @Column(name = OrgPermissionConstants.COL_PERMISSION_NAME,
            nullable = false,
            columnDefinition = OrgPermissionConstants.PERMISSION_NAME_DEFINITION)
    String permissionName;

    @Column(name = OrgPermissionConstants.COL_DESCRIPTION,
            columnDefinition = OrgPermissionConstants.DESCRIPTION_DEFINITION)
    String description;
}
