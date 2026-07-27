package com.restaurant.crm.modules.erp.order.service.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface GroupCartSseService {


    SseEmitter createEmitter(String sessionId);


    void broadcast(String sessionId, String eventName, Object data);
}
