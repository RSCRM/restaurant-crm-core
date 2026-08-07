package com.restaurant.crm.modules.erp.organization.specification;

import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.OrganizationSearchRequest;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationStatus;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrganizationSpecification {

    private OrganizationSpecification() {}

    public static Specification<Organization> build(OrganizationSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId, String currentUserId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base: exclude deleted
            predicates.add(cb.notEqual(root.get("status"), OrganizationStatus.DELETED));

            // ── Search fields (OR) ──
            if (request != null) {
                List<Predicate> searchPredicates = new ArrayList<>();

                if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
                    String pattern = "%" + request.getOrganizationName().trim().toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(root.get("organizationName")), pattern));
                }

                if (request.getTaxCode() != null && !request.getTaxCode().isBlank()) {
                    String pattern = "%" + request.getTaxCode().trim().toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(root.get("taxCode")), pattern));
                }

                if (request.getPhone() != null && !request.getPhone().isBlank()) {
                    String pattern = "%" + request.getPhone().trim() + "%";
                    searchPredicates.add(cb.like(root.get("phone"), pattern));
                }

                if (request.getEmail() != null && !request.getEmail().isBlank()) {
                    String pattern = "%" + request.getEmail().trim().toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(root.get("email")), pattern));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

                // ── Filter fields (AND) ──
                if (request.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), request.getStatus()));
                }

                if (request.getAddress() != null && !request.getAddress().isBlank()) {
                    String pattern = "%" + request.getAddress().trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("address")), pattern));
                }
            }

            // ── Data scope filters (AND) ──
            if (currentUserId != null) {
                // SELF scope: user is owner → find organizations where user has OWNER Employee
                Subquery<String> ownerSubquery = query.subquery(String.class);
                Root<Employee> empRoot = ownerSubquery.from(Employee.class);
                ownerSubquery.select(empRoot.get("organization").get("id"))
                        .where(cb.and(
                                cb.equal(empRoot.get("user").get("id"), currentUserId),
                                cb.equal(empRoot.get("orgRole").get("roleName"), OrgRoleConstants.OWNER_ROLE)
                        ));
                predicates.add(root.get("id").in(ownerSubquery));
            } else if (dataScopeBranchId != null) {
                // BRANCH scope: organization that contains this branch
                Subquery<String> subquery = query.subquery(String.class);
                Root<OrganizationBranch> branchRoot = subquery.from(OrganizationBranch.class);
                subquery.select(branchRoot.get("organization").get("id"))
                        .where(cb.equal(branchRoot.get("id"), dataScopeBranchId));
                predicates.add(root.get("id").in(subquery));
            } else if (dataScopeOrgId != null) {
                // ORGANIZATION scope: only this organization
                predicates.add(cb.equal(root.get("id"), dataScopeOrgId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Same as {@link #build} but excludes organizations that have an active subscription.
     */
    public static Specification<Organization> buildWithoutActiveSubscription(OrganizationSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId, String currentUserId) {

        Specification<Organization> base = build(request, dataScopeOrgId, dataScopeBranchId, currentUserId);

        return base.and((root, query, cb) -> {
            Subquery<String> subquery = query.subquery(String.class);
            Root<LicenseSubscription> lsRoot = subquery.from(LicenseSubscription.class);
            subquery.select(lsRoot.get("organizationId"))
                    .where(cb.equal(lsRoot.get("status"), SubscriptionStatus.ACTIVE));
            return cb.not(root.get("id").in(subquery));
        });
    }
}
