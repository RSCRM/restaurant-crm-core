package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.model.GroupQrPayload;


public interface GroupQrTokenService {


    String generate(GroupQrPayload payload, long ttlSeconds);


    GroupQrPayload verify(String token);
}
