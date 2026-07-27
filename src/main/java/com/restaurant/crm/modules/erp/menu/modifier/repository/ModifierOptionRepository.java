package com.restaurant.crm.modules.erp.menu.modifier.repository;

import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.Optional;
=======
import java.util.Collection;
import java.util.List;
>>>>>>> origin/dev

@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, String> {

<<<<<<< HEAD
    /** Looks up an option scoped to a branch via its group (uc-c-05, NFR-07 guard). */
    Optional<ModifierOption> findByIdAndModifierGroupBranchId(String id, String branchId);
=======
    /**
     * All options for the given group ids in one query, ordered by optionName (uc-c-04).
     * Fetched in a single call to avoid N+1 across modifier groups.
     */
    List<ModifierOption> findByModifierGroupIdInOrderByOptionNameAsc(Collection<String> groupIds);
>>>>>>> origin/dev
}
