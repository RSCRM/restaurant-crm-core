package com.restaurant.crm.modules.erp.repository;

import com.restaurant.crm.modules.erp.entity.OrganizationBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationBranchRepository extends JpaRepository<OrganizationBranch, String> {
    Optional<OrganizationBranch> findByIdAndOrganization_Owner_Id(String branchId, String ownerId);
}
