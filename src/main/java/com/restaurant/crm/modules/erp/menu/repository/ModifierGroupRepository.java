package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, String> {

    List<ModifierGroup> findByProduct_Branch_IdOrderByGroupNameAsc(String branchId);

    List<ModifierGroup> findByProduct_IdOrderByGroupNameAsc(String productId);

    long countByProduct_Id(String productId);

    boolean existsByProduct_IdAndGroupName(String productId, String groupName);

    boolean existsByProduct_IdAndGroupNameAndIdNot(String productId, String groupName, String id);
}
