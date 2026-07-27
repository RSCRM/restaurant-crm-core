package com.restaurant.crm.modules.erp.menu.modifier.repository;

import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, String> {

    /** Looks up an option scoped to a branch via its group (uc-c-05, NFR-07 guard). */
    Optional<ModifierOption> findByIdAndModifierGroupBranchId(String id, String branchId);
}
