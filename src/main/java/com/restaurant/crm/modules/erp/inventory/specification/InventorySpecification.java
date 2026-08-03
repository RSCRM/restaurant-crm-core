package com.restaurant.crm.modules.erp.inventory.specification;

import com.restaurant.crm.modules.erp.inventory.dto.request.InventorySearchRequest;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {

    private InventorySpecification() {}

    public static Specification<Inventory> build(
        String branchId,
        InventorySearchRequest request
    ) {

        Specification<Inventory> spec =
            (root, query, cb) ->
                cb.equal(
                    root.get("ingredient")
                        .get("branch")
                        .get("id"),
                    branchId
                );

        if (request == null) {
            return spec;
        }

        if (request.getIngredientName() != null
            && !request.getIngredientName().isBlank()) {

            String pattern =
                "%" + request.getIngredientName().trim().toLowerCase() + "%";

            spec = spec.and((root, query, cb) ->
                cb.like(
                    cb.lower(
                        root.get("ingredient")
                            .get("ingredientName")
                    ),
                    pattern
                ));
        }

        if (request.getStatus() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), request.getStatus()));
        }

        if (request.getQuantityFrom() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(
                    root.get("quantity"),
                    request.getQuantityFrom()));
        }

        if (request.getQuantityTo() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(
                    root.get("quantity"),
                    request.getQuantityTo()));
        }

        if (request.getMinimumQuantityFrom() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(
                    root.get("minimumQuantity"),
                    request.getMinimumQuantityFrom()));
        }

        if (request.getMinimumQuantityTo() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(
                    root.get("minimumQuantity"),
                    request.getMinimumQuantityTo()));
        }

        if (request.getCreatedAtFrom() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    request.getCreatedAtFrom()));
        }

        if (request.getCreatedAtTo() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    request.getCreatedAtTo()));
        }

        return spec;
    }
}