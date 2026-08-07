package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String>, JpaSpecificationExecutor<Organization> {

    List<Organization> findByStatus(OrganizationStatus status);

    Page<Organization> findByStatus(
            OrganizationStatus status,
            Pageable pageable
    );

    List<Organization> findByOrganizationNameContainingIgnoreCase(String organizationName);

    Page<Organization> findByOrganizationNameContainingIgnoreCase(
            String organizationName,
            Pageable pageable
    );

    Optional<Organization> findByTaxCode(String taxCode);

    boolean existsByTaxCode(String taxCode);

    Page<Organization> findById(
        String id,
        Pageable pageable
    );
}
