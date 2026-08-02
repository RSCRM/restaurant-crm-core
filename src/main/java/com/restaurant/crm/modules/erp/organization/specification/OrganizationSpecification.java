package com.restaurant.crm.modules.erp.organization.specification;

import com.restaurant.crm.modules.erp.organization.dto.request.OrganizationSearchRequest;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class OrganizationSpecification {

    private OrganizationSpecification() {}

    public static Specification<Organization> build(OrganizationSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId, String currentUserId) {

        Specification<Organization> spec = Specification.where(null);

        // ── Search filters ──
        if (request != null) {
            if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
                String pattern = "%" + request.getOrganizationName().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("organizationName")), pattern));
            }

            if (request.getTaxCode() != null && !request.getTaxCode().isBlank()) {
                String pattern = "%" + request.getTaxCode().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("taxCode")), pattern));
            }

            if (request.getOwnerId() != null && !request.getOwnerId().isBlank()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("owner").get("id"), request.getOwnerId()));
            }

            if (request.getStatus() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getAddress() != null && !request.getAddress().isBlank()) {
                String pattern = "%" + request.getAddress().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("address")), pattern));
            }

            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                String pattern = "%" + request.getPhone().trim() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(root.get("phone"), pattern));
            }

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                String pattern = "%" + request.getEmail().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("email")), pattern));
            }
        }

        // ── Data scope filters ──
        if (currentUserId != null) {
            // SELF scope: user is owner
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("owner").get("id"), currentUserId));
        } else if (dataScopeBranchId != null) {
            // BRANCH scope: organization that contains this branch
            spec = spec.and((root, query, cb) -> {
                Subquery<String> subquery = query.subquery(String.class);
                Root<OrganizationBranch> branchRoot = subquery.from(OrganizationBranch.class);
                subquery.select(branchRoot.get("organization").get("id"))
                        .where(cb.equal(branchRoot.get("id"), dataScopeBranchId));
                return root.get("id").in(subquery);
            });
        } else if (dataScopeOrgId != null) {
            // ORGANIZATION scope: only this organization
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("id"), dataScopeOrgId));
        }

        return spec;
    }
}
