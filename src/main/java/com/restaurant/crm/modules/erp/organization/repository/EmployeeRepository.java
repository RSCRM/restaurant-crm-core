package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Optional<Employee> findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
            String id,
            String organizationId,
            String ownerId
    );

    @Query("""
            SELECT DISTINCT e
            FROM Employee e
            JOIN FETCH e.user u
            LEFT JOIN FETCH e.orgRole r
            LEFT JOIN FETCH e.branch b
            LEFT JOIN FETCH b.organization o
            LEFT JOIN FETCH o.owner ow
            WHERE e.id = :employeeId
            """)
    Optional<Employee> findByIdWithUserRoleAndBranch(
            @Param("employeeId") String employeeId
    );

    @Query("""
            SELECT DISTINCT e
            FROM Employee e
            JOIN FETCH e.user u
            LEFT JOIN FETCH e.orgRole r
            JOIN FETCH e.branch b
            JOIN FETCH b.organization o
            LEFT JOIN FETCH o.owner ow
            WHERE u.id = :userId
              AND b.id = :branchId
              AND o.id = :organizationId
            """)
    Optional<Employee> findByUserIdAndBranchIdAndOrganizationIdWithDetails(
            @Param("userId") String userId,
            @Param("branchId") String branchId,
            @Param("organizationId") String organizationId
    );

    @Query("""
            SELECT DISTINCT e
            FROM Employee e
            JOIN FETCH e.user u
            LEFT JOIN FETCH e.orgRole r
            JOIN FETCH e.branch b
            JOIN FETCH b.organization o
            LEFT JOIN FETCH o.owner ow
            WHERE u.id = :userId
              AND b.id = :branchId
              AND o.id = :organizationId
            """)
    List<Employee> findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
            @Param("userId") String userId,
            @Param("branchId") String branchId,
            @Param("organizationId") String organizationId
    );

    @Query(
            value = """
                    SELECT DISTINCT e
                    FROM Employee e
                    JOIN FETCH e.user u
                    LEFT JOIN FETCH e.orgRole r
                    JOIN FETCH e.branch b
                    JOIN FETCH b.organization o
                    WHERE o.id = :organizationId
                      AND (:branchId IS NULL OR b.id = :branchId)
                      AND (:role IS NULL OR r.id = :role OR r.roleName = :role)
                      AND (:status IS NULL OR e.status = :status)
                      AND (
                            :keyword IS NULL
                            OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR e.phone LIKE CONCAT('%', CAST(:keyword AS string), '%')
                      )
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT e)
                    FROM Employee e
                    JOIN e.user u
                    LEFT JOIN e.orgRole r
                    JOIN e.branch b
                    JOIN b.organization o
                    WHERE o.id = :organizationId
                      AND (:branchId IS NULL OR b.id = :branchId)
                      AND (:role IS NULL OR r.id = :role OR r.roleName = :role)
                      AND (:status IS NULL OR e.status = :status)
                      AND (
                            :keyword IS NULL
                            OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR LOWER(e.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                            OR e.phone LIKE CONCAT('%', CAST(:keyword AS string), '%')
                      )
                    """
    )
    Page<Employee> searchByOrganization(
            @Param("organizationId") String organizationId,
            @Param("branchId") String branchId,
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("status") EmployeeStatus status,
            Pageable pageable
    );
}
