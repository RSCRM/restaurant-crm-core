package com.restaurant.crm.modules.erp.table.mapper;

import com.restaurant.crm.modules.erp.table.dto.response.RestaurantTableResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableManagementMapper {

    TableAreaResponse toAreaResponse(TableArea area);

    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "areaName", source = "area.areaName")
    RestaurantTableResponse toTableResponse(RestaurantTable table);
}
