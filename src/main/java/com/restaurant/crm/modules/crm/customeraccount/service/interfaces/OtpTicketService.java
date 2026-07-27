package com.restaurant.crm.modules.crm.customeraccount.service.interfaces;

import java.time.Instant;
import java.util.Optional;

import com.restaurant.crm.modules.crm.customeraccount.model.OtpTicketPayload;

/**
 * Signs and verifies the OTP ticket that proves a customer passed OTP at a specific table (uc-c-03).
 * Same crypto contract as the uc-c-02 QR tokens (SignedJWT + HS512, per-branch HMAC secret).
 */
public interface OtpTicketService {

    /**
     * Issues an OTP ticket bound to the phone + branch + table context (uc-c-03).
     * The ticket is short-lived and consumed by {@code OtpTicketVerifier} in uc-c-02.
     *
     * @return the serialized ticket and its exact expiry
     */
    IssuedTicket issue(String customerPhone, String branchId, String tableId);

    /**
     * Verifies a ticket's signature, expiry and {@code type}, returning its trusted payload (uc-c-03).
     * Does NOT compare phone/branch/table — that is the caller's (adapter's) job.
     *
     * @return the payload if the ticket is structurally valid; empty otherwise (never throws for bad tickets)
     */
    Optional<OtpTicketPayload> verify(String otpTicket);

    /** A signed OTP ticket together with its server-authoritative expiry. */
    record IssuedTicket(String token, Instant expiresAt) {
    }
}
