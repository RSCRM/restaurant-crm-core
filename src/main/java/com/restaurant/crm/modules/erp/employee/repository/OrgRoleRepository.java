package com.restaurant.crm.modules.erp.employee.repository;

import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgRoleRepository extends JpaRepository<OrgRole, String> {

    Optional<OrgRole> findByRoleName(String roleName);

    @Query("select (count(p) > 0) from OrgRole r join r.orgPermissions p " +
           "where r.roleName = :roleName and p.permissionName = :permissionName")
    boolean existsPermissionByRoleName(@Param("roleName") String roleName,
                                       @Param("permissionName") String permissionName);
}
