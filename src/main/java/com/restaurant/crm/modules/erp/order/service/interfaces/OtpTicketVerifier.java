package com.restaurant.crm.modules.erp.order.service.interfaces;


public interface OtpTicketVerifier {


    boolean isValid(String customerPhone, String branchId, String tableId, String otpTicket);
}
