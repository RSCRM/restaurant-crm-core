package com.restaurant.crm.modules.crm.customeraccount.service.interfaces;

import com.restaurant.crm.modules.crm.customeraccount.model.OtpRequestResult;


public interface CustomerOtpService {


    OtpRequestResult request(String customerPhone, String branchId, String tableId);


    String verify(String customerPhone, String branchId, String tableId, String otpCode);
}
