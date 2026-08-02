package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgPermissionRepository extends JpaRepository<OrgPermission, String> {
    boolean existsByPermissionName(String permissionName);
}
