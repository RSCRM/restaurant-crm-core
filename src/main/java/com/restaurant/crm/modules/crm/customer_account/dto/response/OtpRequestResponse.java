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
 * Result of requesting an OTP (uc-c-03). Never contains the OTP code itself,
 * in any profile. {@code maskedPhone} hides the middle digits.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpRequestResponse {

    String maskedPhone;
    Instant expiresAt;
    Instant resendAvailableAt;
    Integer attemptsAllowed;
}
