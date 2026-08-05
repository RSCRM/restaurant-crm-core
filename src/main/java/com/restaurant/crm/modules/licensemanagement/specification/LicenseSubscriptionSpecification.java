package com.restaurant.crm.modules.licensemanagement.specification;

import com.restaurant.crm.modules.licensemanagement.dto.request.SubscriptionSearchRequest;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LicenseSubscriptionSpecification {

    private LicenseSubscriptionSpecification() {}

    public static Specification<LicenseSubscription> build(String organizationId,
            SubscriptionSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base: filter by organization
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (request != null) {
                // -- Search fields (OR) - on License entity via subquery --
                List<Predicate> searchPredicates = new ArrayList<>();

                if (request.getLicenseName() != null && !request.getLicenseName().isBlank()) {
                    String pattern = "%" + request.getLicenseName().trim().toLowerCase() + "%";
                    Subquery<String> subquery = query.subquery(String.class);
                    Root<License> licenseRoot = subquery.from(License.class);
                    subquery.select(licenseRoot.get("id"))
                            .where(cb.like(cb.lower(licenseRoot.get("name")), pattern));
                    searchPredicates.add(root.get("licenseId").in(subquery));
                }

                if (request.getLicenseCode() != null && !request.getLicenseCode().isBlank()) {
                    String pattern = "%" + request.getLicenseCode().trim().toLowerCase() + "%";
                    Subquery<String> subquery = query.subquery(String.class);
                    Root<License> licenseRoot = subquery.from(License.class);
                    subquery.select(licenseRoot.get("id"))
                            .where(cb.like(cb.lower(licenseRoot.get("code")), pattern));
                    searchPredicates.add(root.get("licenseId").in(subquery));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

                // -- Filter fields (AND) --
                if (request.getPriceFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getPriceFrom()));
                }
                if (request.getPriceTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getPriceTo()));
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

                if (request.getStartDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), request.getStartDateFrom()));
                }
                if (request.getStartDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), request.getStartDateTo()));
                }
                if (request.getEndDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), request.getEndDateFrom()));
                }
                if (request.getEndDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), request.getEndDateTo()));
                }

                if (request.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), request.getStatus()));
                }
                if (request.getBillingCycle() != null) {
                    predicates.add(cb.equal(root.get("billingCycle"), request.getBillingCycle()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
