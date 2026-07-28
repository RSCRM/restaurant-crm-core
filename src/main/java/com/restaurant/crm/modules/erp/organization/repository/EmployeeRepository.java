package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByUserIdAndStatus(String userId, EmployeeStatus status);

    Optional<Employee> findByIdAndUserId(String id, String userId);

    @Query("""
            SELECT e
            FROM Employee e
            JOIN FETCH e.user u
            LEFT JOIN FETCH e.orgRole r
            LEFT JOIN FETCH e.branch b
            LEFT JOIN FETCH b.organization o
            LEFT JOIN FETCH o.owner owner
            WHERE e.id = :employeeId
            """)
    Optional<Employee> findByIdWithUserRoleAndBranch(@Param("employeeId") String employeeId);
}
