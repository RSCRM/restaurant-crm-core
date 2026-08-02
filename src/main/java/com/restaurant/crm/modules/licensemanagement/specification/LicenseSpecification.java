package com.restaurant.crm.modules.licensemanagement.specification;

import com.restaurant.crm.modules.licensemanagement.dto.request.LicenseSearchRequest;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import org.springframework.data.jpa.domain.Specification;

public class LicenseSpecification {

    private LicenseSpecification() {}

    public static Specification<License> build(LicenseSearchRequest request) {
        Specification<License> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        if (request == null) {
            return spec;
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            String pattern = "%" + request.getName().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), pattern));
        }

        if (request.getPriceFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), request.getPriceFrom()));
        }
        if (request.getPriceTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), request.getPriceTo()));
        }

        if (request.getBillingCycle() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("billingCycle"), request.getBillingCycle()));
        }

        if (request.getMaxBranchFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("maxBranch"), request.getMaxBranchFrom()));
        }
        if (request.getMaxBranchTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("maxBranch"), request.getMaxBranchTo()));
        }

        if (request.getMaxEmployeeFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("maxEmployee"), request.getMaxEmployeeFrom()));
        }
        if (request.getMaxEmployeeTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("maxEmployee"), request.getMaxEmployeeTo()));
        }

        if (request.getCreatedAtFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAtFrom()));
        }
        if (request.getCreatedAtTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedAtTo()));
        }

        if (request.getStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), request.getStatus()));
        }

        return spec;
    }
}
