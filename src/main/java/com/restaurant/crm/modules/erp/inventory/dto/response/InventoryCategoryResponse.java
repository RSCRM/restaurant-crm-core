package com.restaurant.crm.modules.erp.inventory.dto.response;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryCategoryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryCategoryResponse {
    String id;
    String branchId;
    String categoryName;
    String description;
    Instant createdAt;
    Instant updatedAt;
    InventoryCategoryStatus status;
}
