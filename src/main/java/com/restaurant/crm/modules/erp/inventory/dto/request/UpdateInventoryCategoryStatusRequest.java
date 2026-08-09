package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryCategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInventoryCategoryStatusRequest {

    @NotNull
    private InventoryCategoryStatus status;
}
