package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgPermissionResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgRoleResponse;

import java.util.List;

public interface OrganizationRoleService {
    OrgRoleResponse create(CreateOrgRoleRequest request);
    OrgRoleResponse update(String id, UpdateOrgRoleRequest request);
    List<OrgRoleResponse> listByOrganization(String organizationId);
    OrgRoleResponse get(String id);
    List<OrgPermissionResponse> listPermissions();
}
