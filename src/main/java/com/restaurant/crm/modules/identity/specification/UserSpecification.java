package com.restaurant.crm.modules.identity.specification;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.dto.request.UserSearchRequest;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> build(UserSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId, String currentUserId) {

        Specification<User> spec = (root, query, cb) ->
                cb.notEqual(root.get("status"), UserStatus.DELETED);

        // ── Search filters ──
        if (request != null) {
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                String pattern = "%" + request.getUsername().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("username")), pattern));
            }

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                String pattern = "%" + request.getEmail().trim().toLowerCase() + "%";
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("email")), pattern));
            }

            if (request.getStatus() != null) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("roles", JoinType.INNER).get("roleName"), request.getRoleName()));
            }
        }

        // ── Data scope filters ──
        if (currentUserId != null) {
            // SELF scope
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("id"), currentUserId));
        } else if (dataScopeBranchId != null) {
            // BRANCH scope: users with active employee in this branch
            spec = spec.and((root, query, cb) -> {
                Subquery<String> subquery = query.subquery(String.class);
                Root<Employee> empRoot = subquery.from(Employee.class);
                subquery.select(empRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.equal(empRoot.get("branch").get("id"), dataScopeBranchId),
                                cb.equal(empRoot.get("status"), EmployeeStatus.ACTIVE)
                        ));
                return root.get("id").in(subquery);
            });
        } else if (dataScopeOrgId != null) {
            // ORGANIZATION scope: users with active employee in this organization
            spec = spec.and((root, query, cb) -> {
                Subquery<String> subquery = query.subquery(String.class);
                Root<Employee> empRoot = subquery.from(Employee.class);
                subquery.select(empRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.equal(empRoot.get("branch").get("organization").get("id"), dataScopeOrgId),
                                cb.equal(empRoot.get("status"), EmployeeStatus.ACTIVE)
                        ));
                return root.get("id").in(subquery);
            });
        }

        return spec;
    }
}
