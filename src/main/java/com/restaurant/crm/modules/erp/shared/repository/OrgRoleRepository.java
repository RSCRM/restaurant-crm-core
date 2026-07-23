package com.restaurant.crm.modules.erp.shared.repository;

import com.restaurant.crm.modules.erp.shared.entity.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgRoleRepository extends JpaRepository<OrgRole, String> {
    Optional<OrgRole> findByOrganization_IdAndRoleName(String organizationId, String roleName);

    boolean existsByOrganization_IdAndRoleName(String organizationId, String roleName);
}
