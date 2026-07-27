package com.restaurant.crm.modules.crm.point_wallet.dto.response;

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
public class CustomerPointResponse {
    String id;
    String customerId;
    String restaurantId;
    Integer currentPoints;
    Integer lifetimePoints;
    Instant updatedAt;
}
