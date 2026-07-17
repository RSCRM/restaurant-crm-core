package com.restaurant.crm.modules.identity.repository;

import com.restaurant.crm.modules.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    boolean existsByPermissionName(String permissionName);
    Optional<Permission> findByPermissionName(String permissionName);
}
