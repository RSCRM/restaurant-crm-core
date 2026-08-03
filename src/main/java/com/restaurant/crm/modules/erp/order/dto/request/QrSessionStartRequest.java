package com.restaurant.crm.modules.erp.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


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

    /**
     * Proof of OTP for phones not yet on file. Optional: a returning customer (phone already
     * exists in {@code Customer}) skips OTP entirely, so this may be null/blank for them.
     */
    String otpTicket;
}
