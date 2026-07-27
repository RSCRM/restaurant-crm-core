package com.restaurant.crm.modules.licensemanagement.repository;

import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseSubscriptionRepository extends JpaRepository<LicenseSubscription, String> {

    boolean existsByOrganizationIdAndStatus(String organizationId, com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus status);

    Page<LicenseSubscription> findByLicenseId(String licenseId, Pageable pageable);
}
