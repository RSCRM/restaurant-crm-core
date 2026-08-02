package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.QrResolveRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionJoinRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrResolveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.QrSessionResponse;


public interface QrSessionService {


    QrResolveResponse resolve(QrResolveRequest request);

    QrSessionResponse start(QrSessionStartRequest request);


    QrSessionResponse join(QrSessionJoinRequest request);


    QrSessionResponse getCurrent();

    QrSessionResponse refreshGroupQr();


    QrSessionResponse heartbeat();


    void bindOrder(String sessionId, String orderId);
}
