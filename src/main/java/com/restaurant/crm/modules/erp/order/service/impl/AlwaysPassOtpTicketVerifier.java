package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.erp.order.service.interfaces.OtpTicketVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * DEV-ONLY stub: accepts any non-blank OTP ticket (uc-c-02).
 * TODO(uc-c-03): replace with the real ticket verifier (validate signature/TTL and
 * that the ticket is bound to {@code customerPhone}). Guarded by {@code @Profile("dev")}
 * so it can never be active in production.
 */
@Slf4j
@Service
@Profile("dev")
public class AlwaysPassOtpTicketVerifier implements OtpTicketVerifier {

    @Override
    public boolean isValid(String customerPhone, String branchId, String tableId, String otpTicket) {
        log.warn("DEV OtpTicketVerifier active — accepting OTP ticket without real verification. "
                + "This must not run in production (uc-c-03).");
        return otpTicket != null && !otpTicket.isBlank();
    }
}
