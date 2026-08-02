package com.restaurant.crm.modules.crm.customeraccount.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpSender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Non-dev OTP sender placeholder (uc-c-03): there is no real provider yet, so it fails loudly
 * instead of silently dropping OTPs. {@code @Primary} guards against accidental multi-bean setups.
 * TODO(uc-c-03+): wire a real provider — Zalo ZNS / SMS gateway, dispatched via RabbitMQ
 * (SRS §IV). The dependency is not in pom.xml yet; adding it is out of scope for uc-c-03.
 */
@Slf4j
@Service
@Profile("!dev & !local & !default")
public class NoopOtpSender implements OtpSender {

    @Override
    public void send(String phone, String code) {
        log.error("No OTP provider configured — cannot deliver OTP. Wire Zalo ZNS / SMS gateway (uc-c-03 TODO).");
        throw new AppException(ErrorCode.OTP_SEND_FAILED);
    }
}
