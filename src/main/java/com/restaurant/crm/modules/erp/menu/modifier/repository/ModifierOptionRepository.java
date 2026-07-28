package com.restaurant.crm.modules.erp.menu.modifier.repository;

import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, String> {

    
    Optional<ModifierOption> findByIdAndModifierGroupBranchId(String id, String branchId);
  
    List<ModifierOption> findByModifierGroupIdInOrderByOptionNameAsc(Collection<String> groupIds);
}
