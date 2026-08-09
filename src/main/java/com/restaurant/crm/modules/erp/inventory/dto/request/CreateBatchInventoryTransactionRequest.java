package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateBatchInventoryTransactionRequest {

    @NotEmpty
    private List<CreateInventoryTransactionRequest> transactions;
}
