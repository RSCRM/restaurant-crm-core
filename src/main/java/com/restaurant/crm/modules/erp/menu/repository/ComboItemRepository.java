package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, String> {
    List<ComboItem> findByCombo_Id(String comboId);

    boolean existsByCombo_IdAndProduct_Id(String comboId, String productId);
}
