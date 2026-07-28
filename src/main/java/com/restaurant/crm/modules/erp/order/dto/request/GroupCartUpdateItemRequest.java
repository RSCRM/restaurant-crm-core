package com.restaurant.crm.modules.erp.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupCartUpdateItemRequest {

    @NotNull(message = "CART_ITEM_REQUEST_INVALID")
    @Min(value = 1, message = "CART_ITEM_REQUEST_INVALID")
    Integer quantity;

    String note;

    List<String> modifierOptionIds;
}
