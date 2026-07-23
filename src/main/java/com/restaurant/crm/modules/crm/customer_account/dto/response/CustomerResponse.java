package com.restaurant.crm.modules.crm.customer_account.dto.response;

import com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    String id;
    String phone;
    CustomerStatus status;
    Instant createdAt;
}
