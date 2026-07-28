package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.notification.service.interfaces.BranchTenantResolver;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * Resolves a branch's owning organization for the shared notification infrastructure.
 * <p>
 * Lives in the organization module so that {@code common} does not compile against this module's
 * repositories; the notification side only knows the port.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchTenantResolverImpl implements BranchTenantResolver {

    OrganizationBranchRepository organizationBranchRepository;

    @Override
    public String resolveOrganizationId(String branchId) {
        OrganizationBranch branch = organizationBranchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        if (branch.getOrganization() == null) {
            throw new AppException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }
        // Reading the id off a lazy proxy does not trigger initialization.
        return branch.getOrganization().getId();
    }
}