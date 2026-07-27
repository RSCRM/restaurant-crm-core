package com.restaurant.crm.modules.erp.menu.modifier.repository;

import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, String> {

    /** All modifier groups of a branch, stable order by groupName (uc-c-04). */
    List<ModifierGroup> findByBranchIdOrderByGroupNameAsc(String branchId);
}
