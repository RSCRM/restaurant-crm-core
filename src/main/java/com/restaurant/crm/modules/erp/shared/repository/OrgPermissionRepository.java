package com.restaurant.crm.modules.erp.shared.repository;

import com.restaurant.crm.modules.erp.shared.entity.OrgPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgPermissionRepository extends JpaRepository<OrgPermission, String> {
    boolean existsByPermissionCode(String permissionCode);

    Optional<OrgPermission> findByPermissionCode(String permissionCode);
}
