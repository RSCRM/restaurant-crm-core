package com.restaurant.crm.modules.erp.menu.security;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.stereotype.Component;

@Component
public class MenuBranchGuard {

    // owner (orgRole = OWNER) qua thang; employee phai dung branch cua minh
    public void validateBranchAccess(String branchId) {
        if (OrgRoleConstants.OWNER_ROLE.equals(AuthUtils.getOrgRole())) {
            return;
        }
        if (!branchId.equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }
}
