package com.restaurant.crm.modules.crm.customeraccount.service.interfaces;

import java.time.Instant;
import java.util.Optional;

import com.restaurant.crm.modules.crm.customeraccount.model.OtpTicketPayload;


public interface OtpTicketService {


    IssuedTicket issue(String customerPhone, String branchId, String tableId);


    Optional<OtpTicketPayload> verify(String otpTicket);


    record IssuedTicket(String token, Instant expiresAt) {
    }
}
