package com.restaurant.crm.modules.erp.order.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupCartItemResponse {

    String cartItemId;
    String productId;
    String comboId;
    String name;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal lineTotal;
    String note;

    @Builder.Default
    List<GroupCartItemModifierResponse> modifiers = new ArrayList<>();

    String addedByDeviceId;


    String lockedByDeviceId;
}
