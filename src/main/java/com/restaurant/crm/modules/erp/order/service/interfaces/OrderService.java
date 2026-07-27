package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;

import java.util.List;

public interface OrderService {

    CreateOrderResponse create(CreateOrderRequestDto request);

    CancelOrderResponse cancelOrder(String orderId);

    OrderCookingStatusResponse getOrderCookingStatus(String orderId);

    OrderCookingStatusResponse getActiveOrderCookingStatusByTable(String tableId);

    List<CustomerVoucherApplicableResponse> getApplicableVouchers(String orderId);

    void applyVoucher(String orderId, String customerVoucherId);

    void removeVoucher(String orderId);
}
