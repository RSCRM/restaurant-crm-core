package com.restaurant.crm.modules.licensemanagement.repository;

import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseSubscriptionRepository extends JpaRepository<LicenseSubscription, String>, JpaSpecificationExecutor<LicenseSubscription> {

    boolean existsByOrganizationIdAndStatus(String organizationId, com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus status);

    Page<LicenseSubscription> findByLicenseId(String licenseId, Pageable pageable);

    List<LicenseSubscription> findByOrganizationId(String organizationId);
}
