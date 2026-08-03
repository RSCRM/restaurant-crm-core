package com.restaurant.crm.modules.erp.invoice.service.interfaces;

import com.restaurant.crm.modules.erp.invoice.dto.request.CheckoutRequestDto;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse checkout(CheckoutRequestDto request);
    InvoiceResponse getInvoiceDetails(String invoiceId);
    InvoiceResponse getActiveOrderBillDetails(String orderId);
}
