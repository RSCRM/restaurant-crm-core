package com.restaurant.crm.modules.erp.inventory.specification;

import com.restaurant.crm.modules.erp.inventory.dto.request.InventoryTransactionSearchRequest;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.domain.Specification;

public class InventoryTransactionSpecification {

    private InventoryTransactionSpecification() {
    }

    public static Specification<InventoryTransaction> build(
        String branchId,
        InventoryTransactionSearchRequest request
    ) {

        Specification<InventoryTransaction> spec =
            (root, query, cb) ->
                cb.equal(
                    root.get("inventory")
                        .get("branch")
                        .get("id"),
                    branchId
                );

        if (request == null) {
            return spec;
        }

        if (request.getInventoryName() != null
            && !request.getInventoryName().isBlank()) {

            String pattern =
                "%" + request.getInventoryName().trim().toLowerCase() + "%";

            spec = spec.and((root, query, cb) ->
                cb.like(
                    cb.lower(
                        root.get("inventory")
                            .get("inventoryName")
                    ),
                    pattern
                ));
        }

        if (request.getEmployeeName() != null
            && !request.getEmployeeName().isBlank()) {

            String pattern =
                "%" + request.getEmployeeName().trim().toLowerCase() + "%";

            spec = spec.and((root, query, cb) ->
                cb.like(
                    cb.lower(
                        root.get("employee")
                            .get("user")
                            .get("username")
                    ),
                    pattern
                ));
        }

        if (request.getTransactionType() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(
                    root.get("transactionType"),
                    request.getTransactionType()
                ));
        }

        if (request.getTransactionDirection() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(
                    root.get("transactionDirection"),
                    request.getTransactionDirection()
                ));
        }

        if (request.getTransactionTimeFrom() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(
                    root.get("transactionTime"),
                    request.getTransactionTimeFrom()
                ));
        }

        if (request.getTransactionTimeTo() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(
                    root.get("transactionTime"),
                    request.getTransactionTimeTo()
                ));
        }

        return spec;
    }
}