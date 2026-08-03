package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.response.KdsActiveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.KdsItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.WaitingSummaryDto;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.KdsService;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KdsServiceImpl implements KdsService {

    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    RestaurantTableRepository restaurantTableRepository;
    EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public KdsActiveResponse getActiveItems() {
        String branchId = resolveCurrentBranchId();

        // Query PENDING and IN_PROGRESS items
        List<OrderItem> activeOrderItems = orderItemRepository.findByBranchIdAndStatusIn(
                branchId,
                List.of(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS)
        );

        List<KdsItemResponse> waitingItems = new ArrayList<>();
        List<KdsItemResponse> preparingItems = new ArrayList<>();

        for (OrderItem item : activeOrderItems) {
            KdsItemResponse responseDto = mapToKdsItemResponse(item);
            if (item.getStatus() == OrderItemStatus.PENDING) {
                waitingItems.add(responseDto);
            } else if (item.getStatus() == OrderItemStatus.IN_PROGRESS) {
                preparingItems.add(responseDto);
            }
        }

        // Grouping waiting items for summary
        Map<String, WaitingSummaryDto> summaryMap = new HashMap<>();
        for (KdsItemResponse item : waitingItems) {
            String itemKey = (item.getProductId() != null ? item.getProductId() : item.getComboId())
                    + "_" + (item.getNote() != null ? item.getNote().trim().toLowerCase() : "")
                    + "_" + (item.getModifiers() != null ? item.getModifiers().trim().toLowerCase() : "");

            if (summaryMap.containsKey(itemKey)) {
                WaitingSummaryDto existing = summaryMap.get(itemKey);
                existing.setTotalQuantity(existing.getTotalQuantity() + item.getQuantity());
            } else {
                WaitingSummaryDto summary = WaitingSummaryDto.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .comboId(item.getComboId())
                        .comboName(item.getComboName())
                        .note(item.getNote())
                        .modifiers(item.getModifiers())
                        .totalQuantity(item.getQuantity())
                        .build();
                summaryMap.put(itemKey, summary);
            }
        }
        List<WaitingSummaryDto> waitingSummary = new ArrayList<>(summaryMap.values());

        return KdsActiveResponse.builder()
                .waitingSummary(waitingSummary)
                .waitingItems(waitingItems)
                .preparingItems(preparingItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KdsItemResponse> getHistoryItems() {
        String branchId = resolveCurrentBranchId();

        // Define start of today (since 24 hours ago, or start of calendar day. 24 hours ago is safer and standard for shifts)
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);

        List<OrderItem> historyOrderItems = orderItemRepository.findByBranchIdAndStatusInAndUpdatedAtAfter(
                branchId,
                List.of(OrderItemStatus.READY_TO_SERVE, OrderItemStatus.SERVED, OrderItemStatus.CANCELLED),
                since
        );

        List<KdsItemResponse> historyResponses = new ArrayList<>();
        for (OrderItem item : historyOrderItems) {
            historyResponses.add(mapToKdsItemResponse(item));
        }

        return historyResponses;
    }

    private String resolveCurrentBranchId() {
        String employeeId = AuthUtils.getEmployeeId();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (employee.getBranch() == null) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND);
        }
        return employee.getBranch().getId();
    }

    private KdsItemResponse mapToKdsItemResponse(OrderItem item) {
        String productName = null;
        String comboName = null;

        if (item.getProductId() != null) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                productName = product.getProductName();
            }
        } else if (item.getComboId() != null) {
            Combo combo = comboRepository.findById(item.getComboId()).orElse(null);
            if (combo != null) {
                comboName = combo.getComboName();
            }
        }

        String tableNumber = "Take Out";
        String areaName = null;
        if (item.getOrder().getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(item.getOrder().getTableId()).orElse(null);
            if (table != null) {
                tableNumber = table.getTableNumber();
                if (table.getArea() != null) {
                    areaName = table.getArea().getAreaName();
                }
            }
        }

        // Fetch modifiers
        List<OrderItemModifier> orderItemModifiers = orderItemModifierRepository.findByOrderItemId(item.getId());
        List<String> modifierNames = new ArrayList<>();
        for (OrderItemModifier orderItemModifier : orderItemModifiers) {
            ModifierOption option = modifierOptionRepository.findById(orderItemModifier.getModifierOptionId()).orElse(null);
            if (option != null) {
                modifierNames.add(option.getOptionName());
            }
        }
        Collections.sort(modifierNames);
        String modifiersStr = String.join(", ", modifierNames);

        return KdsItemResponse.builder()
                .orderItemId(item.getId())
                .orderId(item.getOrder().getId())
                .orderCode(item.getOrder().getOrderCode())
                .tableId(item.getOrder().getTableId())
                .tableNumber(tableNumber)
                .areaName(areaName)
                .productId(item.getProductId())
                .productName(productName)
                .comboId(item.getComboId())
                .comboName(comboName)
                .quantity(item.getQuantity())
                .note(item.getNote())
                .modifiers(modifiersStr.isEmpty() ? null : modifiersStr)
                .status(item.getStatus())
                .preparedBy(item.getPreparedBy())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
