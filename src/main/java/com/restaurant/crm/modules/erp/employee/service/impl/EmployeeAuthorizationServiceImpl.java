package com.restaurant.crm.modules.erp.employee.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.employee.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.employee.service.interfaces.EmployeeAuthorizationService;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeAuthorizationServiceImpl implements EmployeeAuthorizationService {

    OrganizationBranchRepository organizationBranchRepository;
    OrgRoleRepository orgRoleRepository;

    @Override
    public void authorize(String targetBranchId, String requiredOrgPermission) {
        String actorUserId = AuthUtils.getCurrentUserId();
        String actorEmployeeId = AuthUtils.getEmployeeId();

        if (actorEmployeeId == null) {
            // OWNER path: token khong co employeeId -> phai so huu org cua branch dich
            boolean owns = organizationBranchRepository
                    .findByIdAndOrganization_OwnerId(targetBranchId, actorUserId)
                    .isPresent();
            if (!owns) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return;
        }

        // EMPLOYEE path: cung branch + org_role co permission tuong ung
        boolean sameBranch = targetBranchId != null && targetBranchId.equals(AuthUtils.getBranchId());
        boolean hasPerm = orgRoleRepository.existsPermissionByRoleName(AuthUtils.getOrgRole(), requiredOrgPermission);
        if (!(sameBranch && hasPerm)) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }
}
