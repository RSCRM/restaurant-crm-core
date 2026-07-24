package com.restaurant.crm.modules.erp.inventory.mapper;

import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Inventory toInventory(
        CreateInventoryRequest request
    );

    @Mapping(
        source = "ingredient.id",
        target = "ingredientId"
    )
    InventoryResponse toInventoryResponse(
        Inventory inventory
    );

    @Mapping(target = "id", ignore = true)

    // Quantity should only change through transactions
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateInventory(
        UpdateInventoryRequest request,
        @MappingTarget Inventory inventory
    );
}
