package com.restaurant.crm.modules.erp.order.mapper;

import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.enums.CustomerOrderStage;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Maps the internal {@code OrderCookingStatusResponse} to the customer tracking DTO (uc-c-06).
 * Drops {@code customerPhone} (not present on the target) and enriches each line with the
 * customer stage. {@code hasActiveOrder} and {@code summary} are filled by the service.
 */
@Mapper(componentModel = "spring")
public interface CustomerOrderTrackingMapper {

    @Mapping(target = "hasActiveOrder", ignore = true)
    @Mapping(target = "summary", ignore = true)
    @Mapping(target = "orderStatus", source = "status")
    CustomerOrderTrackingResponse toTracking(OrderCookingStatusResponse order);

    @Mapping(target = "customerStage", source = "status", qualifiedByName = "toStage")
    @Mapping(target = "stageOrder", source = "status", qualifiedByName = "toStageOrder")
    CustomerOrderTrackingItemResponse toItem(OrderItemCookingStatusResponse item);

    List<CustomerOrderTrackingItemResponse> toItems(List<OrderItemCookingStatusResponse> items);

    @Named("toStage")
    default CustomerOrderStage toStage(OrderItemStatus status) {
        return status == null ? null : CustomerOrderStage.from(status);
    }

    @Named("toStageOrder")
    default Integer toStageOrder(OrderItemStatus status) {
        return status == null ? null : CustomerOrderStage.from(status).getStageOrder();
    }
}
