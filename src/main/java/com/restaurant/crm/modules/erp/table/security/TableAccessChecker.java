package com.restaurant.crm.modules.erp.table.security;

import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("tableAccessChecker")
public class TableAccessChecker {

    // Owner manages directly; manager must also hold the requested granular permission.
    public boolean canManage(String permission) {
        String role = AuthUtils.getOrgRole();
        if ("OWNER".equals(role)) {
            return true;
        }
        if (!"MANAGER".equals(role)) {
            return false;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
