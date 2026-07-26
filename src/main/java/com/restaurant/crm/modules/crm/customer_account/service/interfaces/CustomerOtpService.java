package com.restaurant.crm.modules.crm.customer_account.service.interfaces;

import com.restaurant.crm.modules.crm.customer_account.dto.request.OtpRequestRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.request.OtpVerifyRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.response.OtpRequestResponse;
import com.restaurant.crm.modules.crm.customer_account.dto.response.OtpVerifyResponse;

/**
 * Phone + OTP customer identification (uc-c-03, BR-CST-ACC-02).
 * OTP lives entirely in Redis; no customer row is created here (only a read to check LOCKED).
 */
public interface CustomerOtpService {

    /**
     * Requests an OTP for a phone at the scanned table (uc-c-03).
     * Requires a valid TABLE QR, enforces per-phone cooldown / lock and per-table rate limit,
     * then sends the code. The response never contains the code.
     */
    OtpRequestResponse request(OtpRequestRequest request);

    /**
     * Verifies an entered OTP against the scanned table (uc-c-03) and, on success, issues an
     * {@code otpTicket} bound to phone + branch + table. Wrong attempts are counted atomically;
     * 3 wrong attempts invalidate the code and lock the phone for 15 minutes.
     */
    OtpVerifyResponse verify(OtpVerifyRequest request);
}
