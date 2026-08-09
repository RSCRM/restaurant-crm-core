package com.restaurant.crm.modules.erp.invoice.dto.request;

import com.restaurant.crm.modules.erp.invoice.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutRequestDto {

    @NotBlank(message = "Order ID is required")
    String orderId;

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod;

    String note;

    String voucherCode;
}
