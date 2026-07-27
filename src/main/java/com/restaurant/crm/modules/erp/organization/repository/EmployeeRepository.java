package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByUserIdAndStatus(String userId, EmployeeStatus status);

    Optional<Employee> findByIdAndUserId(String id, String userId);

    Optional<Employee> findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
            String id,
            String organizationId,
            String ownerId
    );

    Optional<Employee> findByUser_IdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
            String userId,
            String organizationId,
            String ownerId
    );
}
