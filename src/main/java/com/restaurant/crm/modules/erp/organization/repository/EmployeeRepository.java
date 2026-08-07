package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByUserIdAndStatus(String userId, EmployeeStatus status);

    Optional<Employee> findByIdAndUserId(String id, String userId);
    Optional<Employee> findByIdAndBranch_Id(String id, String branchId);

    Optional<Employee> findFirstByUser_Id(String userId);

    Optional<Employee> findFirstByUser_IdAndBranch_Id(String userId, String branchId);
    Optional<Employee> findFirstByUser_IdAndBranch_Organization_Id(
            String userId, String organizationId);

    List<Employee> findByBranch_Organization_Id(String organizationId);
    List<Employee> findByBranch_Id(String branchId);

    List<Employee> findByUser_IdIn(Collection<String> userIds);
    List<Employee> findByUser_IdInAndBranch_Id(Collection<String> userIds, String branchId);
    List<Employee> findByUser_IdInAndBranch_Organization_Id(
            Collection<String> userIds, String organizationId);

    List<Employee> findByBranch_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
            String branchId, EmployeeStatus status, String excludedRole);

    List<Employee> findByBranch_Organization_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
            String organizationId, EmployeeStatus status, String excludedRole);

    Optional<Employee> findByIdAndBranch_IdAndOrgRole_RoleNameNot(
            String id, String branchId, String excludedRole);

    Optional<Employee> findByUser_IdAndBranch_Organization_IdAndBranch_Organization_OwnerId(
            String userId,
            String organizationId,
            String ownerId
    );
}
