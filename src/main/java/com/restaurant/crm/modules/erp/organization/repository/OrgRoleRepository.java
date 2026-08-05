package com.restaurant.crm.modules.erp.organization.repository;

import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgRoleRepository extends JpaRepository<OrgRole, String> {
    Optional<OrgRole> findByRoleName(String roleName);

    List<OrgRole> findByOrganization_Id(String organizationId);

    boolean existsByOrganization_IdAndRoleName(String organizationId, String roleName);

    boolean existsByOrganization_IdAndRoleNameAndIdNot(String organizationId, String roleName, String id);
}
