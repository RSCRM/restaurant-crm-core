package com.restaurant.crm.modules.erp.menu.security;

import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("menuAccessChecker")
public class MenuAccessChecker {

    // owner (orgRole = OWNER) -> qua cong; nguoc lai phai co authority = permission
    public boolean canManage(String permission) {
        if (OrgRoleConstants.OWNER_ROLE.equals(AuthUtils.getOrgRole())) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
