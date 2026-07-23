package com.restaurant.crm.modules.licensemanagement.repository;

import com.restaurant.crm.modules.licensemanagement.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

    boolean existsByCode(String code);

    Optional<License> findByIdAndDeletedAtIsNull(String id);
}
