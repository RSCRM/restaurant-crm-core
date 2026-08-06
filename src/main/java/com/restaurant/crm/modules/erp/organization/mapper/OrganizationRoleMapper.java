package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.response.OrgPermissionResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgRoleResponse;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganizationRoleMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "permissions", source = "orgPermissions")
    OrgRoleResponse toOrgRoleResponse(OrgRole role);

    OrgPermissionResponse toOrgPermissionResponse(OrgPermission permission);

    List<OrgPermissionResponse> toOrgPermissionResponses(Collection<OrgPermission> permissions);
}
