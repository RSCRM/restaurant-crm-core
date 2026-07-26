package com.restaurant.crm.modules.erp.order.service.interfaces;

/**
 * Plug point for OTP verification (uc-c-02 defines the contract; uc-c-03 implements it).
 * uc-c-02 does NOT implement OTP itself — it only consumes the ticket that uc-c-03 issues
 * once the customer has passed OTP for the given phone number.
 */
public interface OtpTicketVerifier {

    /**
     * Checks that {@code otpTicket} is a valid proof that OTP passed for {@code customerPhone} (uc-c-03).
     *
     * @return {@code true} if the ticket is valid and bound to the phone; {@code false} otherwise
     */
    boolean isValid(String customerPhone, String otpTicket);
}
