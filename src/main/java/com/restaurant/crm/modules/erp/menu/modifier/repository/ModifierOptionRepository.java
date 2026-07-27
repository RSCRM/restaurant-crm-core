package com.restaurant.crm.modules.erp.menu.modifier.repository;

import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, String> {

    /**
     * All options for the given group ids in one query, ordered by optionName (uc-c-04).
     * Fetched in a single call to avoid N+1 across modifier groups.
     */
    List<ModifierOption> findByModifierGroupIdInOrderByOptionNameAsc(Collection<String> groupIds);
}
