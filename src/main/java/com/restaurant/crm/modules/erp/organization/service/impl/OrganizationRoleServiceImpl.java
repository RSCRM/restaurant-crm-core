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
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationRoleMapper;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationRoleService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationRoleServiceImpl implements OrganizationRoleService {

    OrgRoleRepository orgRoleRepository;
    OrgPermissionRepository orgPermissionRepository;
    OrganizationRepository organizationRepository;
    OrganizationRoleMapper mapper;

    @Override
    @Transactional
    public OrgRoleResponse create(CreateOrgRoleRequest request) {
        Organization org = requireOwnedOrganization(request.getOrganizationId());
        if (orgRoleRepository.existsByOrganization_IdAndRoleName(org.getId(), request.getRoleName())) {
            throw new AppException(ErrorCode.ORG_ROLE_NAME_EXISTS);
        }
        OrgRole role = OrgRole.builder()
                .organization(org)
                .roleName(request.getRoleName())
                .dataScope(request.getDataScope())
                .orgPermissions(resolvePermissions(request.getPermissionIds()))
                .build();
        return mapper.toOrgRoleResponse(orgRoleRepository.save(role));
    }

    @Override
    @Transactional
    public OrgRoleResponse update(String id, UpdateOrgRoleRequest request) {
        OrgRole role = orgRoleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORG_ROLE_NOT_FOUND));
        String orgId = role.getOrganization() != null ? role.getOrganization().getId() : null;
        requireOwnedOrganization(orgId);
        if (!role.getRoleName().equals(request.getRoleName())
                && orgRoleRepository.existsByOrganization_IdAndRoleNameAndIdNot(orgId, request.getRoleName(), id)) {
            throw new AppException(ErrorCode.ORG_ROLE_NAME_EXISTS);
        }
        role.setRoleName(request.getRoleName());
        role.setDataScope(request.getDataScope());
        role.setOrgPermissions(resolvePermissions(request.getPermissionIds()));
        return mapper.toOrgRoleResponse(orgRoleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgRoleResponse> listByOrganization(String organizationId) {
        requireOwnedOrganization(organizationId);
        return orgRoleRepository.findByOrganization_Id(organizationId)
                .stream()
                .filter(role -> !OrgRoleConstants.OWNER_ROLE.equals(role.getRoleName()))
                .map(mapper::toOrgRoleResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrgRoleResponse get(String id) {
        OrgRole role = orgRoleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORG_ROLE_NOT_FOUND));
        requireOwnedOrganization(role.getOrganization() != null ? role.getOrganization().getId() : null);
        return mapper.toOrgRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgPermissionResponse> listPermissions() {
        return mapper.toOrgPermissionResponses(orgPermissionRepository.findAll());
    }

    private Organization requireOwnedOrganization(String organizationId) {
        if (organizationId == null) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return organizationRepository.findByIdAndOwnerId(organizationId, AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
    }

    private Set<OrgPermission> resolvePermissions(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Set.of();
        }
        List<OrgPermission> found = orgPermissionRepository.findAllById(permissionIds);
        if (found.size() != permissionIds.stream().distinct().count()) {
            throw new AppException(ErrorCode.ORG_PERMISSION_NOT_FOUND);
        }
        return Set.copyOf(found);
    }
}
