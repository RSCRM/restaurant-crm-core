package com.restaurant.crm.modules.erp.shared.repository;

import com.restaurant.crm.modules.erp.shared.entity.LicenseContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseContractRepository extends JpaRepository<LicenseContract, String> {
    Page<LicenseContract> findAllByOrganization_OwnerId(String ownerId, Pageable pageable);

    Optional<LicenseContract> findFirstByOrganization_IdAndOrganization_OwnerIdOrderByCreatedAtDesc(
            String organizationId,
            String ownerId
    );
}
