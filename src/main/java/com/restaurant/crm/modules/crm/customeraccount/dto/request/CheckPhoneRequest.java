package com.restaurant.crm.modules.crm.customeraccount.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckPhoneRequest {
    @NotBlank
    String phone;
}
