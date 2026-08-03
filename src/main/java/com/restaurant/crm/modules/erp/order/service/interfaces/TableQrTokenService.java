package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.model.TableQrPayload;


public interface TableQrTokenService {


    String generate(TableQrPayload payload);


    TableQrPayload verify(String token);
}
