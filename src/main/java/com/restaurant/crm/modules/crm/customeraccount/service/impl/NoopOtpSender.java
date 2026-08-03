package com.restaurant.crm.modules.crm.customeraccount.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpSender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


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
