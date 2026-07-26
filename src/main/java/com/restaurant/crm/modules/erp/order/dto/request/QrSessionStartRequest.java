package com.restaurant.crm.modules.erp.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Body of {@code POST /public/customer/qr/session} — OWNER opens a session (uc-c-02).
 * Called after uc-c-03's OTP passes; {@code otpTicket} is the proof of that.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrSessionStartRequest {

    @NotBlank(message = "TQR_TOKEN_INVALID")
    String qrToken;

    @NotBlank(message = "CUSTOMER_PHONE_REQUIRED")
    String customerPhone;

    @NotBlank(message = "TQR_OTP_TICKET_INVALID")
    String otpTicket;
}
