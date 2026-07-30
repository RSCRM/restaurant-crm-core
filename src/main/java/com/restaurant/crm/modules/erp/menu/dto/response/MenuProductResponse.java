package com.restaurant.crm.modules.erp.menu.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * A single product in the customer menu (uc-c-04).
 * {@code available} is derived from the entity status; {@code branchId} is intentionally omitted.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuProductResponse {

    String productId;
    String productName;
    String description;
    BigDecimal price;
    String imageUrl;
    boolean available;
    Boolean requiresPreparation;
}
