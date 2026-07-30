package com.restaurant.crm.modules.erp.table.dto.request;

import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateTableRequest {

    @NotBlank(message = "RESTAURANT_TABLE_AREA_REQUIRED")
    String areaId;

    @NotBlank(message = "RESTAURANT_TABLE_NUMBER_REQUIRED")
    @Size(max = 20)
    String tableNumber;

    @NotNull(message = "RESTAURANT_TABLE_CAPACITY_REQUIRED")
    @Min(value = 1, message = "RESTAURANT_TABLE_CAPACITY_INVALID")
    Integer capacity;

    RestaurantTableStatus status;

    Integer positionX;
    Integer positionY;
}
