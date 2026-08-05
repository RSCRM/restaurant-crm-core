package com.restaurant.crm.modules.erp.inventory.mapper;

import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "transactionTime", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    InventoryTransaction toInventoryTransaction(
        CreateInventoryTransactionRequest request
    );

    @Mapping(
        source = "inventory.id",
        target = "inventoryId"
    )
    @Mapping(
        source = "employee.id",
        target = "employeeId"
    )
    @Mapping(
        source = "inventory.inventoryName",
        target = "inventoryName"
    )
    @Mapping(source = "employee.user.username", target = "employeeName")
    InventoryTransactionResponse toInventoryTransactionResponse(
        InventoryTransaction transaction
    );
}