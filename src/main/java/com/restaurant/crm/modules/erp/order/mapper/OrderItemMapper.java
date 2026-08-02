package com.restaurant.crm.modules.erp.order.mapper;

import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "id", target = "orderItemId")
    @Mapping(source = "order.id", target = "orderId")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
