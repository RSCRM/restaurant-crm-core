package com.restaurant.crm.modules.erp.service.impl;

import com.restaurant.crm.modules.erp.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.entity.OrderItem;
import com.restaurant.crm.modules.erp.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.mapper.KitchenOrderItemMapper;
import com.restaurant.crm.modules.erp.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.service.interfaces.KitchenDisplayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KitchenDisplayServiceImpl implements KitchenDisplayService {

    /** Only items still on the kitchen board are returned (state-machine doc section 4). */
    private static final List<OrderItemStatus> KITCHEN_QUEUE_STATUSES =
            List.of(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS);

    OrderItemRepository orderItemRepository;
    KitchenOrderItemMapper kitchenOrderItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<KitchenOrderItemResponse> getKitchenQueue(String branchId) {
        List<OrderItem> items = orderItemRepository.findKitchenQueue(branchId, KITCHEN_QUEUE_STATUSES);
        return kitchenOrderItemMapper.toResponseList(items);
    }
}
