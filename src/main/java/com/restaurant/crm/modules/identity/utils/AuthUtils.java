package com.restaurant.crm.modules.identity.utils;

import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AuthUtils {

    private AuthUtils() {}

    public static String getCurrentUserId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_USER_ID);
    }

    public static String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
    }

    public static String getTokenType() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_TYPE);
    }

    public static String getOrganizationId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID);
    }

    public static String getBranchId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_BRANCH_ID);
    }

    public static String getEmployeeId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_EMPLOYEE_ID);
    }

    public static String getOrgRole() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_ORG_ROLE);
    }

    public static OrgDataScope getDataScope() {
        String dataScope = getJwtClaimAsString(JwtClaimSetConstant.CLAIM_DATA_SCOPE);
        if (dataScope == null) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
        return OrgDataScope.valueOf(dataScope);
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    public static boolean hasOrgRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    // ==== QR ordering / customer session claims (uc-c-02) ====

    public static String getSessionId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_SESSION_ID);
    }

    public static boolean hasAuthority(String authority) {
        return authority != null && getAuthorities().contains(authority);
    }

    public static boolean hasAnyAuthority(String... authorities) {
        if (authorities == null) {
            return false;
        }

        Set<String> currentAuthorities = getAuthorities();
        for (String authority : authorities) {
            if (currentAuthorities.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    public static String requireOrganizationId() {
        String organizationId = getOrganizationId();
        if (organizationId == null || organizationId.isBlank()) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
        return organizationId;
    }

    private static String getJwtClaimAsString(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaims().get(claimName);
            if (claim != null) {
                return claim.toString();
            }
            return null;
        }
        throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
    }
}
