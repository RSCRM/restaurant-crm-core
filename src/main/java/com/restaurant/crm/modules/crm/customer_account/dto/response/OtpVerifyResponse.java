package com.restaurant.crm.modules.crm.customer_account.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Result of a successful OTP verification (uc-c-03): the {@code otpTicket} to hand to
 * {@code POST /public/customer/qr/session} (uc-c-02), plus its expiry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpVerifyResponse {

    String otpTicket;
    Instant ticketExpiresAt;
}
