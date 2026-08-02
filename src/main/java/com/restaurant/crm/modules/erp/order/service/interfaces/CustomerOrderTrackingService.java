package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface CustomerOrderTrackingService {


    CustomerOrderTrackingResponse getCurrentCookingStatus();


    SseEmitter subscribeToCookingStatus();
}
