package com.restaurant.crm.modules.erp.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/** Body of {@code POST /public/customer/qr/session/join} — MEMBER joins via GROUP QR (uc-c-02). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrSessionJoinRequest {

    @NotBlank(message = "TQR_TOKEN_INVALID")
    String groupQrToken;
}
