package com.restaurant.crm.modules.erp.repository;

import com.restaurant.crm.modules.erp.entity.LicenseContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseContractRepository extends JpaRepository<LicenseContract, String> {
    Page<LicenseContract> findAllByOrganization_Owner_Id(String ownerId, Pageable pageable);

    Optional<LicenseContract> findFirstByOrganization_IdAndOrganization_Owner_IdOrderByCreatedAtDesc(
            String organizationId,
            String ownerId
    );
}
