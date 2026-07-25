package com.restaurant.crm.modules.erp.qr_ordering.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/** Body of {@code POST /public/customer/qr/resolve} — the scanned TABLE QR (uc-c-02). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrResolveRequest {

    @NotBlank(message = "TQR_TOKEN_INVALID")
    String qrToken;
}
