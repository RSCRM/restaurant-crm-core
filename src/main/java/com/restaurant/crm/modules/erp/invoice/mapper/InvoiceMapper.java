package com.restaurant.crm.modules.erp.invoice.mapper;

import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceResponse;
import com.restaurant.crm.modules.erp.invoice.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    @Mapping(source = "order.branchId", target = "branchId")
    @Mapping(source = "order.tableId", target = "tableId")
    @Mapping(source = "order.customerPhone", target = "customerPhone")
    InvoiceResponse toInvoiceResponse(Invoice invoice);
}
