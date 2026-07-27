package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByUserIdAndStatus(String userId, EmployeeStatus status);

    Optional<Employee> findByIdAndUserId(String id, String userId);

    Optional<Employee> findByIdAndOrgRole_RoleNameAndBranch_Organization_Owner_Id(
            String id,
            String roleName,
            String ownerId
    );

    Page<Employee> findAllByOrgRole_RoleNameAndBranch_IdAndBranch_Organization_Owner_Id(
            String roleName,
            String branchId,
            String ownerId,
            Pageable pageable
    );

    Page<Employee> findAllByOrgRole_RoleNameAndBranch_Organization_Owner_Id(
            String roleName,
            String ownerId,
            Pageable pageable
    );

    boolean existsByBranch_IdAndOrgRole_RoleNameAndStatus(
            String branchId,
            String roleName,
            EmployeeStatus status
    );

    boolean existsByBranch_IdAndOrgRole_RoleNameAndStatusAndIdNot(
            String branchId,
            String roleName,
            EmployeeStatus status,
            String id
    );
}
