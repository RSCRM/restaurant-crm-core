package com.restaurant.crm.modules.erp.inventory.mapper;

import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryCategoryMapper {

    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "branch", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    InventoryCategory toInventoryCategory(
        CreateInventoryCategoryRequest request
    );

    @Mapping(
        source = "branch.id",
        target = "branchId"
    )
    InventoryCategoryResponse toInventoryCategoryResponse(
        InventoryCategory inventoryCategory
    );

    @Mapping(target = "id", ignore = true)

    // Don't allow changing branch
    @Mapping(target = "branch", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateInventoryCategory(
        UpdateInventoryCategoryRequest request,
        @MappingTarget InventoryCategory inventoryCategory
    );
}