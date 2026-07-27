package com.restaurant.crm.modules.erp.order.service.interfaces;

/**
 * Plug point for OTP verification (uc-c-02 defines the contract; uc-c-03 implements it).
 * uc-c-02 does NOT implement OTP itself — it only consumes the ticket that uc-c-03 issues
 * once the customer has passed OTP for the given phone number at a specific table.
 */
public interface OtpTicketVerifier {

    /**
     * Checks that {@code otpTicket} proves OTP passed for {@code customerPhone} AND that the ticket
     * was issued for this exact {@code branchId}/{@code tableId} context (uc-c-03).
     * Binding the ticket to branch+table prevents a ticket earned at one table from opening a
     * session at another table or branch (NFR-07) — the verifier must reject any mismatch.
     *
     * @return {@code true} only if the ticket is valid and bound to the given phone, branch and table;
     *         {@code false} otherwise
     */
    boolean isValid(String customerPhone, String branchId, String tableId, String otpTicket);
}
