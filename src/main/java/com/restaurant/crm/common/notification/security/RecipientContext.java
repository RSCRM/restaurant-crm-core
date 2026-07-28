package com.restaurant.crm.common.notification.security;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.notification.constants.NotificationConstants;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * Everything the notification module is allowed to know about the caller.
 * <p>
 * Tenant identity is derived <strong>exclusively</strong> from the access token. No endpoint accepts
 * an organization or branch id from the request: a caller that can name its own tenant can name
 * somebody else's.
 *
 * @param organizationId tenant root, always present
 * @param branchId       may be null for org-level contexts that have not selected a branch
 * @param employeeId     identifies the recipient, always present
 * @param orgRole        role name inside the organization, may be null
 * @param permissions    granted authorities; never empty (see {@code NO_PERMISSION_SENTINEL})
 */
public record RecipientContext(
        String organizationId,
        String branchId,
        String employeeId,
        String orgRole,
        Set<String> permissions
) {

    /**
     * Guarantees the authority set is immutable and never empty, so callers can hand it straight to
     * a JPQL {@code IN} clause without a special case.
     */
    public RecipientContext {
        permissions = (permissions == null || permissions.isEmpty())
                ? Set.of(NotificationConstants.NO_PERMISSION_SENTINEL)
                : Set.copyOf(permissions);
    }

    /**
     * Builds the context from the current access token.
     *
     * @throws AppException if the token carries no tenant/employee identity — an identity token or a
     *                      customer session token cannot address the staff notification feed
     */
    public static RecipientContext fromSecurityContext() {
        String organizationId = readClaim(AuthUtils::getOrganizationId);
        String employeeId = readClaim(AuthUtils::getEmployeeId);

        if (organizationId == null || employeeId == null) {
            throw new AppException(ErrorCode.NOTIFICATION_TENANT_CONTEXT_MISSING);
        }

        return new RecipientContext(
                organizationId,
                readClaim(AuthUtils::getBranchId),
                employeeId,
                readClaim(AuthUtils::getOrgRole),
                currentAuthorities()
        );
    }

    /** Branch id, refusing contexts that have not selected one. */
    public String requireBranchId() {
        if (branchId == null) {
            throw new AppException(ErrorCode.NOTIFICATION_TENANT_CONTEXT_MISSING);
        }
        return branchId;
    }

    private static Set<String> currentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        Set<String> authorities = new HashSet<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }
        return authorities;
    }

    /** {@code AuthUtils} throws when the token is not a JWT; treat that as "claim absent". */
    private static String readClaim(ClaimReader reader) {
        try {
            return reader.read();
        } catch (AppException e) {
            return null;
        }
    }

    @FunctionalInterface
    private interface ClaimReader {
        String read();
    }
}