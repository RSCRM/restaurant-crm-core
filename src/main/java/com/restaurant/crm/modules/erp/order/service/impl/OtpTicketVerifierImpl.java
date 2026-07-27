package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpTicketService;
import com.restaurant.crm.modules.crm.customeraccount.utils.PhoneNumberUtils;
import com.restaurant.crm.modules.erp.order.service.interfaces.OtpTicketVerifier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Production OTP ticket verifier (uc-c-03): the real implementation of the uc-c-02 plug point.
 * Delegates signature/expiry/type checks to {@code OtpTicketService} (crm), then confirms the
 * ticket is bound to the exact phone + branch + table the caller supplies (NFR-07).
 * {@code @Profile("!dev")} so it never coexists with the dev stub {@code AlwaysPassOtpTicketVerifier}.
 */
@Service
@Profile("!dev")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpTicketVerifierImpl implements OtpTicketVerifier {

    OtpTicketService otpTicketService;

    @Override
    public boolean isValid(String customerPhone, String branchId, String tableId, String otpTicket) {
        // The ticket stores the normalized phone; normalize the caller's phone before comparing.
        String normalizedPhone = PhoneNumberUtils.normalize(customerPhone);
        return otpTicketService.verify(otpTicket)
                .map(payload -> Objects.equals(payload.customerPhone(), normalizedPhone)
                        && Objects.equals(payload.branchId(), branchId)
                        && Objects.equals(payload.tableId(), tableId))
                .orElse(false);
    }
}
