package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.ComboItemModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemModifierOptionRepository extends JpaRepository<ComboItemModifierOption, String> {
    List<ComboItemModifierOption> findByComboItem_Id(String comboItemId);

    void deleteByComboItem_Id(String comboItemId);
}
