package com.restaurant.crm.modules.identity.utils;

import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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

    // ==== QR ordering / customer session claims (uc-c-02) ====

    public static String getSessionId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_SESSION_ID);
    }

    public static String getDeviceId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_DEVICE_ID);
    }

    public static String getSessionRole() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_SESSION_ROLE);
    }

    public static String getTableId() {
        return getJwtClaimAsString(JwtClaimSetConstant.CLAIM_TABLE_ID);
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
