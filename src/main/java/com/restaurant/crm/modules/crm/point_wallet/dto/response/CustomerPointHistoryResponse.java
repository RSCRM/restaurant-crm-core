package com.restaurant.crm.modules.crm.point_wallet.dto.response;

import com.restaurant.crm.modules.crm.point_wallet.enums.PointTransactionType;
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
public class CustomerPointHistoryResponse {
    String id;
    String customerId;
    String restaurantId;
    PointTransactionType transactionType;
    Integer pointsChanged;
    String referenceId;
    Instant createdAt;
}
