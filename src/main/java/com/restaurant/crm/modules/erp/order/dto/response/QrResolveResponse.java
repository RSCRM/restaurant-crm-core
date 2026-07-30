package com.restaurant.crm.modules.erp.order.dto.response;

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
public class QrResolveResponse {

    String organizationId;
    String branchId;
    String branchName;
    String areaName;
    String tableNumber;
    Integer capacity;
    String tableStatus;

    boolean joinable;

    boolean hasActiveSession;
}
