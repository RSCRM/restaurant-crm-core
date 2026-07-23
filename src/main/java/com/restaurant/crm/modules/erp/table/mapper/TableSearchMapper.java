package com.restaurant.crm.modules.erp.table.mapper;

import com.restaurant.crm.modules.erp.table.dto.response.TableSearchResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableSearchMapper {

    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "areaName", source = "area.areaName")
    TableSearchResponse toResponse(RestaurantTable table);
}

