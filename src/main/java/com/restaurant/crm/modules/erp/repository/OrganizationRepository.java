package com.restaurant.crm.modules.erp.repository;

import com.restaurant.crm.modules.erp.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {
    Page<Organization> findAllByOwner_Id(String ownerId, Pageable pageable);

    Optional<Organization> findByIdAndOwner_Id(String organizationId, String ownerId);
}
