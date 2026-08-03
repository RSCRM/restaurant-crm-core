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

    Optional<OrganizationBranch> findByIdAndOrganization_OwnerId(
            String id,
            String ownerId
    );

    List<OrganizationBranch> findByManagerId(String employeeId);

    @Query("""
            SELECT b
            FROM OrganizationBranch b
            JOIN FETCH b.organization o
            JOIN FETCH o.owner owner
            WHERE b.id = :branchId
              AND owner.id = :ownerId
            """)
    Optional<OrganizationBranch> findByIdAndOwnerIdWithManager(
            @Param("branchId") String branchId,
            @Param("ownerId") String ownerId
    );
}
