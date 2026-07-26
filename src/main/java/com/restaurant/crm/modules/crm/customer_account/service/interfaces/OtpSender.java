package com.restaurant.crm.modules.crm.customer_account.service.interfaces;

/**
 * Delivers an OTP code to the customer (uc-c-03). Real providers (Zalo ZNS / SMS gateway)
 * are not wired yet; implementations are selected by Spring profile.
 */
public interface OtpSender {

    /**
     * Sends {@code code} to {@code phone} (uc-c-03).
     *
     * @throws com.restaurant.crm.common.exception.AppException {@code OTP_SEND_FAILED} on delivery failure
     */
    void send(String phone, String code);
}
