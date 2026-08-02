package com.restaurant.crm.modules.crm.customeraccount.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CheckPhoneResponse {
    String phone;
    boolean exists;
}
