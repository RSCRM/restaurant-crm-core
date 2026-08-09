package com.restaurant.crm.modules.erp.organization.specification;

import com.restaurant.crm.modules.erp.organization.dto.request.BranchSearchRequest;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BranchSpecification {

    private BranchSpecification() {}

    public static Specification<OrganizationBranch> build(
            String orgId, BranchSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by organization
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));

            if (request != null) {
                // Search fields (OR)
                List<Predicate> searchPredicates = new ArrayList<>();

                if (request.getBranchName() != null && !request.getBranchName().isBlank()) {
                    String pattern = "%" + request.getBranchName().trim().toLowerCase() + "%";
                    searchPredicates.add(
                            cb.like(cb.lower(root.get("branchName")), pattern));
                }

                if (request.getPhone() != null && !request.getPhone().isBlank()) {
                    String pattern = "%" + request.getPhone().trim() + "%";
                    searchPredicates.add(cb.like(root.get("phone"), pattern));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

                // Filter fields (AND)
                if (request.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), request.getStatus()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
