package com.restaurant.crm.modules.erp.organization.security;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.stereotype.Component;

@Component
public class OrgRoleGuard {

    // dam bao nguoi take action la owner (theo orgRole tu claim), khong phai nhan vien
    public void requireOwner() {
        if (!OrgRoleConstants.OWNER_ROLE.equals(AuthUtils.getOrgRole())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }
}
