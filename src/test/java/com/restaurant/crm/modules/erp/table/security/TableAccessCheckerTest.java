package com.restaurant.crm.modules.erp.table.security;

import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class TableAccessCheckerTest {

    private final TableAccessChecker checker = new TableAccessChecker();

    @Test
    void ownerCanManageWithoutEmployeePermission() {
        try (var authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getOrgRole).thenReturn("OWNER");
            assertTrue(checker.canManage("RESTAURANT_TABLE_UPDATE"));
        }
    }

    @Test
    void managerNeedsRequestedPermission() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("manager", null, "RESTAURANT_TABLE_UPDATE")
        );
        try (var authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getOrgRole).thenReturn("MANAGER");
            assertTrue(checker.canManage("RESTAURANT_TABLE_UPDATE"));
            assertFalse(checker.canManage("RESTAURANT_TABLE_DELETE"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void otherRolesCannotManageEvenWithPermission() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("waiter", null, "RESTAURANT_TABLE_UPDATE")
        );
        try (var authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getOrgRole).thenReturn("WAITER");
            assertFalse(checker.canManage("RESTAURANT_TABLE_UPDATE"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
