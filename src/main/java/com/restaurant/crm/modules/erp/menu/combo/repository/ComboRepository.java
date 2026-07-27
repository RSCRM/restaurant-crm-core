package com.restaurant.crm.modules.erp.menu.combo.repository;

import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, String> {
    Optional<Combo> findByIdAndBranchId(String comboId, String branchId);

    /** All combos of a branch, stable order by comboName (uc-c-04). */
    List<Combo> findByBranchIdOrderByComboNameAsc(String branchId);
}
