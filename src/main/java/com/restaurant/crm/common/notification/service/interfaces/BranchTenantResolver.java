package com.restaurant.crm.common.notification.service.interfaces;

/**
 * Port letting the shared notification infrastructure resolve a branch's owning organization
 * without compiling against the organization module's repositories.
 * <p>
 * Implemented in {@code modules.erp.organization}.
 */
public interface
BranchTenantResolver {

    /**
     * @param branchId branch to resolve
     * @return the owning organization id
     * @throws com.restaurant.crm.common.exception.AppException if the branch does not exist
     */
    String resolveOrganizationId(String branchId);
}