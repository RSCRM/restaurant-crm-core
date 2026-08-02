package com.restaurant.crm.modules.licensemanagement.specification;

import com.restaurant.crm.modules.licensemanagement.dto.request.LicenseSearchRequest;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LicenseSpecification {

    private LicenseSpecification() {}

    public static Specification<License> build(LicenseSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base: exclude deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // ── Search fields (OR) ──
            List<Predicate> searchPredicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isBlank()) {
                String pattern = "%" + request.getName().trim().toLowerCase() + "%";
                searchPredicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (!searchPredicates.isEmpty()) {
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            // ── Filter fields (AND) ──
            if (request.getPriceFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getPriceFrom()));
            }
            if (request.getPriceTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getPriceTo()));
            }

            if (request.getBillingCycle() != null) {
                predicates.add(cb.equal(root.get("billingCycle"), request.getBillingCycle()));
            }

            if (request.getMaxBranchFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxBranch"), request.getMaxBranchFrom()));
            }
            if (request.getMaxBranchTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxBranch"), request.getMaxBranchTo()));
            }

            if (request.getMaxEmployeeFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxEmployee"), request.getMaxEmployeeFrom()));
            }
            if (request.getMaxEmployeeTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxEmployee"), request.getMaxEmployeeTo()));
            }

            if (request.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
            }
            if (request.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
