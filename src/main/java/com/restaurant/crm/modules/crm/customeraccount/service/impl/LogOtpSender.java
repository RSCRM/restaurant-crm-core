package com.restaurant.crm.modules.crm.customeraccount.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpSender;
import com.restaurant.crm.modules.crm.customeraccount.utils.PhoneNumberUtils;

/**
 * DEV-ONLY OTP sender (uc-c-03): logs the code so developers can test without a provider.
 * Guarded by {@code @Profile("dev")} — the code is NEVER logged in any other profile.
 */
@Slf4j
@Service
@Profile("dev")
public class LogOtpSender implements OtpSender {

    @Override
    public void send(String phone, String code) {
        log.warn("DEV OtpSender — no real SMS/ZNS provider. OTP for {} is {} (dev only, do not use in prod).",
                PhoneNumberUtils.mask(phone), code);
    }
}
