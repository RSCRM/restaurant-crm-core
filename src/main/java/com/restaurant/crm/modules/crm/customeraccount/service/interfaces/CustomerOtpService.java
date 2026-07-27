package com.restaurant.crm.modules.crm.customer_account.service.interfaces;

import com.restaurant.crm.modules.crm.customer_account.model.OtpRequestResult;

/**
 * Phone + OTP customer identification (uc-c-03, BR-CST-ACC-02).
 * QR-agnostic on purpose: branch/table are passed in as already-trusted values, so this domain
 * service never imports anything from the erp domain. OTP lives entirely in Redis; no customer
 * row is created here (only a read to check LOCKED). The web layer verifies the TABLE QR
 * and supplies branch/table.
 */
public interface CustomerOtpService {

    /**
     * Requests an OTP for a phone at a table (uc-c-03).
     * Enforces per-phone cooldown / lock and per-table rate limit, then sends the code.
     * The result never contains the code.
     */
    OtpRequestResult request(String customerPhone, String branchId, String tableId);

    /**
     * Verifies an entered OTP against a table (uc-c-03) and, on success, returns a freshly issued
     * {@code otpTicket} bound to phone + branch + table. Wrong attempts are counted atomically;
     * 3 wrong attempts invalidate the code and lock the phone for 15 minutes.
     *
     * @return the serialized OTP ticket
     */
    String verify(String customerPhone, String branchId, String tableId, String otpCode);
}
