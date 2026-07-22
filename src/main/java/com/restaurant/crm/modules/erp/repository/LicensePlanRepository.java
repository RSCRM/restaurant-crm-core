package com.restaurant.crm.modules.erp.repository;

import com.restaurant.crm.modules.erp.entity.LicensePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicensePlanRepository extends JpaRepository<LicensePlan, String> {
}
