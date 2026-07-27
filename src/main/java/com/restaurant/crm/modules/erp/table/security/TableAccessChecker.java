package com.restaurant.crm.modules.erp.table.security;

import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("tableAccessChecker")
public class TableAccessChecker {

    // owner-context (token khong co employeeId) -> qua cong; nguoc lai phai co authority = permission
    public boolean canManage(String permission) {
        if (AuthUtils.getEmployeeId() == null) {
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
