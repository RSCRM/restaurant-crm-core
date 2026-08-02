package com.restaurant.crm.modules.erp.order;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.response.KdsActiveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.KdsItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.WaitingSummaryDto;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.service.impl.KdsServiceImpl;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class KdsServiceImplTests {

    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    OrderItemModifierRepository orderItemModifierRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ComboRepository comboRepository;
    @Mock
    ModifierOptionRepository modifierOptionRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    EmployeeRepository employeeRepository;

    @InjectMocks
    KdsServiceImpl kdsService;

    @Test
    public void testGetActiveItems_Success() {
        String employeeId = "emp-1";
        String branchId = "branch-1";

        OrganizationBranch branch = OrganizationBranch.builder().id(branchId).build();
        Employee employee = Employee.builder().id(employeeId).branch(branch).build();

        Order order1 = Order.builder().id("order-1").orderCode("ORD01").tableId("table-1").build();
        Order order2 = Order.builder().id("order-2").orderCode("ORD02").tableId("table-2").build();
        Order order3 = Order.builder().id("order-3").orderCode("ORD03").tableId("table-3").build();

        OrderItem item1 = OrderItem.builder()
                .id("item-1")
                .order(order1)
                .productId("prod-1")
                .quantity(2)
                .status(OrderItemStatus.PENDING)
                .note("Không cay")
                .createdAt(Instant.now())
                .build();

        OrderItem item2 = OrderItem.builder()
                .id("item-2")
                .order(order2)
                .productId("prod-1")
                .quantity(3)
                .status(OrderItemStatus.PENDING)
                .note("Không cay")
                .createdAt(Instant.now())
                .build();

        OrderItem item3 = OrderItem.builder()
                .id("item-3")
                .order(order3)
                .productId("prod-2")
                .quantity(1)
                .status(OrderItemStatus.IN_PROGRESS)
                .note("Ít dầu")
                .createdAt(Instant.now())
                .build();

        List<OrderItem> activeItems = new ArrayList<>();
        activeItems.add(item1);
        activeItems.add(item2);
        activeItems.add(item3);

        Product product1 = Product.builder().id("prod-1").productName("Mỳ Ý").build();
        Product product2 = Product.builder().id("prod-2").productName("Cơm Chiên").build();

        TableArea area = TableArea.builder().id("area-1").areaName("Khu A").build();
        RestaurantTable table1 = RestaurantTable.builder().id("table-1").tableNumber("01").area(area).build();
        RestaurantTable table2 = RestaurantTable.builder().id("table-2").tableNumber("02").area(area).build();
        RestaurantTable table3 = RestaurantTable.builder().id("table-3").tableNumber("03").area(area).build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getEmployeeId).thenReturn(employeeId);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(orderItemRepository.findByBranchIdAndStatusIn(branchId, List.of(OrderItemStatus.PENDING, OrderItemStatus.IN_PROGRESS)))
                    .thenReturn(activeItems);

            when(productRepository.findById("prod-1")).thenReturn(Optional.of(product1));
            when(productRepository.findById("prod-2")).thenReturn(Optional.of(product2));

            when(restaurantTableRepository.findById("table-1")).thenReturn(Optional.of(table1));
            when(restaurantTableRepository.findById("table-2")).thenReturn(Optional.of(table2));
            when(restaurantTableRepository.findById("table-3")).thenReturn(Optional.of(table3));

            when(orderItemModifierRepository.findByOrderItemId("item-1")).thenReturn(Collections.emptyList());
            when(orderItemModifierRepository.findByOrderItemId("item-2")).thenReturn(Collections.emptyList());
            when(orderItemModifierRepository.findByOrderItemId("item-3")).thenReturn(Collections.emptyList());

            KdsActiveResponse response = kdsService.getActiveItems();

            assertNotNull(response);
            assertEquals(1, response.getWaitingSummary().size());
            assertEquals(2, response.getWaitingItems().size());
            assertEquals(1, response.getPreparingItems().size());

            WaitingSummaryDto summary = response.getWaitingSummary().get(0);
            assertEquals("prod-1", summary.getProductId());
            assertEquals("Mỳ Ý", summary.getProductName());
            assertEquals(5, summary.getTotalQuantity());
            assertEquals("Không cay", summary.getNote());
        }
    }

    @Test
    public void testGetHistoryItems_Success() {
        String employeeId = "emp-1";
        String branchId = "branch-1";

        OrganizationBranch branch = OrganizationBranch.builder().id(branchId).build();
        Employee employee = Employee.builder().id(employeeId).branch(branch).build();

        Order order = Order.builder().id("order-1").orderCode("ORD01").tableId("table-1").build();
        OrderItem item = OrderItem.builder()
                .id("item-1")
                .order(order)
                .productId("prod-1")
                .quantity(1)
                .status(OrderItemStatus.READY_TO_SERVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Product product = Product.builder().id("prod-1").productName("Mỳ Ý").build();
        TableArea area = TableArea.builder().id("area-1").areaName("Khu A").build();
        RestaurantTable table = RestaurantTable.builder().id("table-1").tableNumber("01").area(area).build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getEmployeeId).thenReturn(employeeId);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(orderItemRepository.findByBranchIdAndStatusInAndUpdatedAtAfter(eq(branchId), any(), any()))
                    .thenReturn(Collections.singletonList(item));

            when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
            when(restaurantTableRepository.findById("table-1")).thenReturn(Optional.of(table));
            when(orderItemModifierRepository.findByOrderItemId("item-1")).thenReturn(Collections.emptyList());

            List<KdsItemResponse> history = kdsService.getHistoryItems();

            assertNotNull(history);
            assertEquals(1, history.size());
            assertEquals("item-1", history.get(0).getOrderItemId());
            assertEquals(OrderItemStatus.READY_TO_SERVE, history.get(0).getStatus());
        }
    }

    @Test
    public void testResolveCurrentBranchId_NoBranch_ThrowsException() {
        String employeeId = "emp-1";
        Employee employee = Employee.builder().id(employeeId).branch(null).build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getEmployeeId).thenReturn(employeeId);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            assertThrows(AppException.class, () -> {
                kdsService.getActiveItems();
            });
        }
    }
}
