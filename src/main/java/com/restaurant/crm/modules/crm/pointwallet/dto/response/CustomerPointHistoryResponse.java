package com.restaurant.crm.modules.crm.pointwallet.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import com.restaurant.crm.modules.crm.pointwallet.enums.PointTransactionType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerPointHistoryResponse {
    String id;
    String customerId;
    String restaurantId;
    PointTransactionType transactionType;
    Integer pointsChanged;
    String referenceId;
    Instant createdAt;
}
