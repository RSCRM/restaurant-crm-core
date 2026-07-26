package com.restaurant.crm.modules.crm.customer_account.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/** Body of {@code POST /public/customer/otp/verify} — TABLE QR + phone + entered code (uc-c-03). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpVerifyRequest {

    @NotBlank(message = "TQR_TOKEN_INVALID")
    String qrToken;

    @NotBlank(message = "CUSTOMER_PHONE_REQUIRED")
    String customerPhone;

    @NotBlank(message = "OTP_INVALID")
    String otpCode;
}
