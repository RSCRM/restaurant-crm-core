package com.restaurant.crm.modules.erp.menu.specification;

import com.restaurant.crm.modules.erp.menu.dto.request.ProductSearchRequest;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> build(ProductSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base: exclude deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (request != null) {
                // -- Search fields (OR) --
                List<Predicate> searchPredicates = new ArrayList<>();

                if (request.getProductName() != null && !request.getProductName().isBlank()) {
                    String pattern = "%" + request.getProductName().trim().toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(root.get("productName")), pattern));
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
            }

            // -- Data scope filters (AND) --
            if (dataScopeBranchId != null) {
                // BRANCH / SELF scope: products in this branch
                predicates.add(cb.equal(root.get("branch").get("id"), dataScopeBranchId));
            } else if (dataScopeOrgId != null) {
                // ORGANIZATION scope: products in any branch of this organization
                predicates.add(cb.equal(root.get("branch").get("organization").get("id"), dataScopeOrgId));
            }
            // ADMIN: no data scope filter

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
