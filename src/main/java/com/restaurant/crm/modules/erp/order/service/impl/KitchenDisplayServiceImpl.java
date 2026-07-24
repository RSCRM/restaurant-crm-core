package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.KitchenDisplayService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KitchenDisplayServiceImpl implements KitchenDisplayService {

    /** Only items still on the kitchen board; READY_TO_SERVE and later drop off. */
    private static final List<OrderItemStatus> KITCHEN_QUEUE_STATUSES =
            List.of(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS);

    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    RestaurantTableRepository restaurantTableRepository;

    @Override
    @Transactional(readOnly = true)
    public List<KitchenOrderItemResponse> getKitchenQueue() {
        String branchId = AuthUtils.getBranchId();
        List<OrderItem> items = orderItemRepository.findKitchenQueue(branchId, KITCHEN_QUEUE_STATUSES);
        if (items.isEmpty()) {
            return List.of();
        }

        // Resolve display names in bulk rather than per item, to keep the board query cheap.
        Map<String, String> productNames = loadProductNames(items);
        Map<String, String> comboNames = loadComboNames(items);
        Map<String, String> tableNumbers = loadTableNumbers(items);

        return items.stream()
                .map(item -> toResponse(item, productNames, comboNames, tableNumbers))
                .toList();
    }

    private KitchenOrderItemResponse toResponse(
            OrderItem item,
            Map<String, String> productNames,
            Map<String, String> comboNames,
            Map<String, String> tableNumbers
    ) {
        return KitchenOrderItemResponse.builder()
                .orderItemId(item.getId())
                .itemName(resolveItemName(item, productNames, comboNames))
                .quantity(item.getQuantity())
                .tableNumber(resolveTableNumber(item, tableNumbers))
                .note(item.getNote())
                .status(item.getStatus())
                .priorityFlag(item.isPriorityFlag())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private String resolveItemName(
            OrderItem item,
            Map<String, String> productNames,
            Map<String, String> comboNames
    ) {
        if (item.getProductId() != null) {
            return productNames.get(item.getProductId());
        }
        if (item.getComboId() != null) {
            return comboNames.get(item.getComboId());
        }
        return null;
    }

    private String resolveTableNumber(OrderItem item, Map<String, String> tableNumbers) {
        String tableId = item.getOrder() == null ? null : item.getOrder().getTableId();
        return tableId == null ? null : tableNumbers.get(tableId);
    }

    private Map<String, String> loadProductNames(List<OrderItem> items) {
        Set<String> ids = collectIds(items, OrderItem::getProductId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Product::getProductName));
    }

    private Map<String, String> loadComboNames(List<OrderItem> items) {
        Set<String> ids = collectIds(items, OrderItem::getComboId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return comboRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Combo::getId, Combo::getComboName));
    }

    private Map<String, String> loadTableNumbers(List<OrderItem> items) {
        Set<String> ids = items.stream()
                .map(OrderItem::getOrder)
                .filter(Objects::nonNull)
                .map(order -> order.getTableId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return restaurantTableRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(RestaurantTable::getId, RestaurantTable::getTableNumber));
    }

    private Set<String> collectIds(List<OrderItem> items, Function<OrderItem, String> idGetter) {
        return items.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
