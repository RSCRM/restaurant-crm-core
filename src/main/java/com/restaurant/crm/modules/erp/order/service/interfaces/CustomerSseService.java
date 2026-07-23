package com.restaurant.crm.modules.erp.order.service.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface CustomerSseService {
    SseEmitter createEmitter(String orderId);
    void broadcastOrderUpdate(String orderId, Object data);
}
