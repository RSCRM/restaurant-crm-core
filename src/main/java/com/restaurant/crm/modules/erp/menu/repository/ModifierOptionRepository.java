package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ModifierOptionRepository extends JpaRepository<ModifierOption, String> {

    List<ModifierOption> findByModifierGroupIdInOrderByOptionNameAsc(Collection<String> groupIds);

    List<ModifierOption> findByModifierGroup_IdOrderByOptionNameAsc(String groupId);

    boolean existsByModifierGroup_IdAndOptionName(String groupId, String optionName);

    boolean existsByModifierGroup_IdAndOptionNameAndIdNot(String groupId, String optionName, String id);

    void deleteByModifierGroup_Id(String groupId);
}
