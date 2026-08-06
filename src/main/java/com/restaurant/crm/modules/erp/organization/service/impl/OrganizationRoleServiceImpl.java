package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgPermissionResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgRoleResponse;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationRoleMapper;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.security.OrgRoleGuard;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationRoleService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationRoleServiceImpl implements OrganizationRoleService {

    OrgRoleRepository orgRoleRepository;
    OrgPermissionRepository orgPermissionRepository;
    OrganizationRepository organizationRepository;
    OrganizationRoleMapper mapper;
    OrgRoleGuard orgRoleGuard;

    @Override
    @Transactional
    public OrgRoleResponse create(CreateOrgRoleRequest request) {
        Organization org = currentOrganization();
        if (orgRoleRepository.existsByOrganization_IdAndRoleName(org.getId(), request.getRoleName())) {
            throw new AppException(ErrorCode.ORG_ROLE_NAME_EXISTS);
        }
        OrgRole role = OrgRole.builder()
                .organization(org)
                .roleName(request.getRoleName())
                .dataScope(OrgDataScope.BRANCH)
                .orgPermissions(resolvePermissions(request.getPermissionIds()))
                .build();
        return mapper.toOrgRoleResponse(orgRoleRepository.save(role));
    }

    @Override
    @Transactional
    public OrgRoleResponse update(String id, UpdateOrgRoleRequest request) {
        OrgRole role = orgRoleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORG_ROLE_NOT_FOUND));
        Organization org = currentOrganization();
        requireRoleInOrganization(role, org);
        if (!role.getRoleName().equals(request.getRoleName())
                && orgRoleRepository.existsByOrganization_IdAndRoleNameAndIdNot(org.getId(), request.getRoleName(), id)) {
            throw new AppException(ErrorCode.ORG_ROLE_NAME_EXISTS);
        }
        role.setRoleName(request.getRoleName());
        role.setDataScope(OrgDataScope.BRANCH);
        role.setOrgPermissions(resolvePermissions(request.getPermissionIds()));
        return mapper.toOrgRoleResponse(orgRoleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgRoleResponse> listOrgRoles() {
        Organization org = organizationFromToken();
        return orgRoleRepository.findByOrganization_Id(org.getId())
                .stream()
                .filter(role -> !OrgRoleConstants.OWNER_ROLE.equals(role.getRoleName()))
                .map(mapper::toOrgRoleResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrgRoleResponse get(String id) {
        OrgRole role = orgRoleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORG_ROLE_NOT_FOUND));
        requireRoleInOrganization(role, organizationFromToken());
        return mapper.toOrgRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgPermissionResponse> listPermissions() {
        return mapper.toOrgPermissionResponses(orgPermissionRepository.findAll());
    }

    // owner-only (create/update): check owner roi lay org tu token
    private Organization currentOrganization() {
        orgRoleGuard.requireOwner();
        return organizationFromToken();
    }

    // view (list/get): chi scope theo organizationId trong token, khong ep owner
    // (authz da gate o @PreAuthorize: ORG_ROLE_MANAGE or EMPLOYEE_ROLE_ASSIGN)
    private Organization organizationFromToken() {
        String orgId = AuthUtils.getOrganizationId();
        if (orgId == null) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
    }

    private void requireRoleInOrganization(OrgRole role, Organization org) {
        if (role.getOrganization() == null || !org.getId().equals(role.getOrganization().getId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    private Set<OrgPermission> resolvePermissions(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        List<OrgPermission> found = orgPermissionRepository.findAllById(permissionIds);
        if (found.size() != permissionIds.stream().distinct().count()) {
            throw new AppException(ErrorCode.ORG_PERMISSION_NOT_FOUND);
        }
        return new HashSet<>(found);
    }
}
