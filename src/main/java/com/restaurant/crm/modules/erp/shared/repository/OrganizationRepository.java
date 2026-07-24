package com.restaurant.crm.modules.erp.shared.repository;

import com.restaurant.crm.modules.erp.shared.entity.Organization;
import com.restaurant.crm.modules.erp.shared.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Page<Organization> findAllByOwnerId(String ownerId, Pageable pageable);

    Optional<Organization> findByIdAndOwnerId(String organizationId, String ownerId);

    Optional<Organization> findByOwnerId(String ownerId);

    boolean existsByOwnerId(String ownerId);

    List<Organization> findByStatus(OrganizationStatus status);

    Page<Organization> findByStatus(OrganizationStatus status, Pageable pageable);

    List<Organization> findByOrganizationNameContainingIgnoreCase(String organizationName);

    Page<Organization> findByOrganizationNameContainingIgnoreCase(String organizationName, Pageable pageable);

    Optional<Organization> findByTaxCode(String taxCode);

    boolean existsByTaxCode(String taxCode);
}
