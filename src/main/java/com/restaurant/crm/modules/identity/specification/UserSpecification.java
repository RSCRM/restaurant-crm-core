package com.restaurant.crm.modules.identity.specification;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.identity.dto.request.UserSearchRequest;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> build(UserSearchRequest request,
            String dataScopeOrgId, String dataScopeBranchId, String currentUserId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base: exclude deleted
            predicates.add(cb.notEqual(root.get("status"), UserStatus.DELETED));

            // ── Search fields (OR) ──
            if (request != null) {
                List<Predicate> searchPredicates = new ArrayList<>();

                if (request.getUsername() != null && !request.getUsername().isBlank()) {
                    String pattern = "%" + request.getUsername().trim().toLowerCase() + "%";
                    searchPredicates.add(cb.like(cb.lower(root.get("username")), pattern));
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

                if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
                    predicates.add(cb.equal(root.join("roles", JoinType.INNER).get("roleName"), request.getRoleName()));
                }
            }

            // ── Data scope filters (AND) ──
            if (currentUserId != null) {
                // SELF scope
                predicates.add(cb.equal(root.get("id"), currentUserId));
            } else if (dataScopeBranchId != null) {
                // BRANCH scope: users with active employee in this branch
                Subquery<String> subquery = query.subquery(String.class);
                Root<Employee> empRoot = subquery.from(Employee.class);
                subquery.select(empRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.equal(empRoot.get("branch").get("id"), dataScopeBranchId),
                                cb.equal(empRoot.get("status"), EmployeeStatus.ACTIVE)
                        ));
                predicates.add(root.get("id").in(subquery));
            } else if (dataScopeOrgId != null) {
                // ORGANIZATION scope: users with active employee in this organization
                Subquery<String> subquery = query.subquery(String.class);
                Root<Employee> empRoot = subquery.from(Employee.class);
                subquery.select(empRoot.get("user").get("id"))
                        .where(cb.and(
                                cb.equal(empRoot.get("branch").get("organization").get("id"), dataScopeOrgId),
                                cb.equal(empRoot.get("status"), EmployeeStatus.ACTIVE)
                        ));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
