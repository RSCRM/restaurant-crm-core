package com.restaurant.crm.modules.erp.table.dto.response;

import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableStatusResponse {
    String id;
    String tableNumber;
    Integer capacity;
    RestaurantTableStatus status;
    Integer positionX;
    Integer positionY;
}

