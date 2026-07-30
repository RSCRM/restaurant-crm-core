package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationBranchRepository extends JpaRepository<OrganizationBranch, String> {

    List<OrganizationBranch> findByOrganizationId(String organizationId);

    Page<OrganizationBranch> findByOrganizationId(
            String organizationId,
            Pageable pageable
    );

    List<OrganizationBranch> findByOrganizationIdAndStatus(
            String organizationId,
            OrganizationBranchStatus status
    );

    Page<OrganizationBranch> findByOrganizationIdAndStatus(
            String organizationId,
            OrganizationBranchStatus status,
            Pageable pageable
    );

    List<OrganizationBranch> findByStatus(OrganizationBranchStatus status);

    Page<OrganizationBranch> findByStatus(
            OrganizationBranchStatus status,
            Pageable pageable
    );

    boolean existsByOrganizationIdAndBranchName(
            String organizationId,
            String branchName
    );

    Optional<OrganizationBranch> findByIdAndOrganization_Owner_Id(
            String id,
            String ownerId
    );

    Optional<OrganizationBranch> findByManager_Id(String employeeId);

    @Query("""
            SELECT b
            FROM OrganizationBranch b
            JOIN FETCH b.organization o
            JOIN FETCH o.owner owner
            LEFT JOIN FETCH b.manager m
            LEFT JOIN FETCH m.user managerUser
            LEFT JOIN FETCH m.orgRole managerRole
            WHERE b.id = :branchId
              AND owner.id = :ownerId
            """)
    Optional<OrganizationBranch> findByIdAndOwnerIdWithManager(
            @Param("branchId") String branchId,
            @Param("ownerId") String ownerId
    );

    @Query("""
            SELECT b
            FROM OrganizationBranch b
            JOIN FETCH b.organization o
            LEFT JOIN FETCH b.manager m
            LEFT JOIN FETCH m.user managerUser
            LEFT JOIN FETCH m.orgRole managerRole
            WHERE b.id = :branchId
              AND o.id = :organizationId
            """)
    Optional<OrganizationBranch> findByIdAndOrganizationIdWithManager(
            @Param("branchId") String branchId,
            @Param("organizationId") String organizationId
    );
}
