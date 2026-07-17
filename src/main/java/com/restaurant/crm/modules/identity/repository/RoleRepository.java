package com.restaurant.crm.modules.identity.repository;

import com.restaurant.crm.modules.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByRoleName(String roleName);
    Optional<Role> findByRoleName(String roleName);
}
