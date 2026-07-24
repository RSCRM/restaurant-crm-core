package com.restaurant.crm.modules.erp.invoice.dto.response;

import com.restaurant.crm.modules.erp.invoice.enums.InvoiceStatus;
import com.restaurant.crm.modules.erp.invoice.enums.PaymentMethod;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
    String id;
    String orderId;
    String invoiceCode;
    String orderCode;
    String branchId;
    String tableId;
    String customerPhone;
    PaymentMethod paymentMethod;
    InvoiceStatus status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    Instant paidAt;
    String note;
    List<InvoiceItemResponse> items;
}
