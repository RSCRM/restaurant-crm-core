package com.restaurant.crm.modules.erp.table.dto.response;

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
public class RestaurantTableResponse {
    String id;
    String areaId;
    String areaName;
    String tableNumber;
    Integer capacity;
    String status;
    Integer positionX;
    Integer positionY;
}
