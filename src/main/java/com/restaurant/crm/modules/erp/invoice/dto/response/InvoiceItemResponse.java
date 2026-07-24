package com.restaurant.crm.modules.erp.invoice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItemResponse {
    String id;
    String productId;
    String comboId;
    String itemName;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal subtotal;
    String note;
    List<InvoiceItemModifierResponse> modifiers;
}
