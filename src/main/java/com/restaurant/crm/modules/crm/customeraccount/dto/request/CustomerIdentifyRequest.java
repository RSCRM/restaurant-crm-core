package com.restaurant.crm.modules.crm.customeraccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CustomerIdentifyRequest {

    @NotBlank(message = "CUSTOMER_PHONE_REQUIRED")
    @Size(min = 9, max = 15, message = "CUSTOMER_PHONE_INVALID")
    String phone;

    @NotBlank(message = "RESTAURANT_ID_REQUIRED")
    String restaurantId;
}
