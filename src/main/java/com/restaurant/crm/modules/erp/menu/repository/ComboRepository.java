package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, String>, JpaSpecificationExecutor<Combo> {
    Optional<Combo> findByIdAndBranchId(String comboId, String branchId);

    List<Combo> findByBranchIdOrderByComboNameAsc(String branchId);

    boolean existsByBranch_IdAndComboName(String branchId, String comboName);

    boolean existsByBranch_IdAndComboNameAndIdNot(String branchId, String comboName, String id);
}
