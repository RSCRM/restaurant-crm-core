package com.restaurant.crm.modules.erp.table.mapper;

import com.restaurant.crm.modules.erp.table.dto.response.TableAreaMapResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableStatusResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TableMapMapper {

    @Mapping(target = "tables", ignore = true)
    TableAreaMapResponse toAreaResponse(TableArea area);

    TableStatusResponse toTableResponse(RestaurantTable table);
}

