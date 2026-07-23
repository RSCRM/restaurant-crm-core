package com.restaurant.crm.modules.erp.mapper;

import com.restaurant.crm.modules.erp.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KitchenOrderItemMapper {

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "tableNumber", source = "order.tableNumber")
    KitchenOrderItemResponse toResponse(OrderItem orderItem);

    List<KitchenOrderItemResponse> toResponseList(List<OrderItem> orderItems);
}
