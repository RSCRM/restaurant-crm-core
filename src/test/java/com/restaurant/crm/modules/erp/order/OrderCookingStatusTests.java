package com.restaurant.crm.modules.erp.order;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.impl.OrderServiceImpl;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.CustomerVoucherService;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCookingStatusTests {

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    OrderItemModifierRepository orderItemModifierRepository;
    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    CustomerSseService customerSseService;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    PointWalletService pointWalletService;
    @Mock
    CustomerVoucherService customerVoucherService;
    @Mock
    CustomerVoucherRepository customerVoucherRepository;
    @Mock
    SseEmitterService sseEmitterService;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    public void testCreateOrder_NewSession_TableAvailable_Success() {
        String branchId = "branch-1";
        String tableId = "table-1";

        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .status(RestaurantTableStatus.AVAILABLE)
                .build();

        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .branchId(branchId)
                .tableId(tableId)
                .orderType(OrderType.DINE_IN)
                .items(Collections.singletonList(
                        CreateOrderItemRequestDto.builder()
                                .productId("prod-1")
                                .quantity(2)
                                .build()
                ))
                .build();

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .tableId(tableId)
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Product product = Product.builder()
                .id("prod-1")
                .price(BigDecimal.valueOf(50000))
                .build();

        when(organizationBranchRepository.existsById(branchId)).thenReturn(true);
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(tableId, branchId)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.findByIdAndBranchId("prod-1", branchId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId("order-1")).thenReturn(Collections.emptyList());

        CreateOrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
        assertEquals(RestaurantTableStatus.OCCUPIED, table.getStatus());
        verify(restaurantTableRepository, times(1)).save(table);
    }

    @Test
    public void testCreateOrder_ExistingSession_TableOccupied_AppendsItemsAndBroadcasts() {
        String branchId = "branch-1";
        String tableId = "table-1";

        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .status(RestaurantTableStatus.OCCUPIED)
                .build();

        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .branchId(branchId)
                .tableId(tableId)
                .orderType(OrderType.DINE_IN)
                .items(Collections.singletonList(
                        CreateOrderItemRequestDto.builder()
                                .productId("prod-2")
                                .quantity(1)
                                .build()
                ))
                .build();

        Order activeOrder = Order.builder()
                .id("order-active")
                .branchId(branchId)
                .tableId(tableId)
                .orderCode("ORD12345")
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        Product product = Product.builder()
                .id("prod-2")
                .productName("Beer")
                .price(BigDecimal.valueOf(25000))
                .build();

        OrderItem existingItem = OrderItem.builder()
                .id("item-1")
                .productId("prod-1")
                .quantity(2)
                .status(OrderItemStatus.SERVED)
                .build();

        OrderItem newItem = OrderItem.builder()
                .id("item-2")
                .productId("prod-2")
                .quantity(1)
                .status(OrderItemStatus.PENDING)
                .build();

        when(organizationBranchRepository.existsById(branchId)).thenReturn(true);
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(tableId, branchId)).thenReturn(true);
        when(orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc(tableId, OrderStatus.PENDING))
                .thenReturn(Optional.of(activeOrder));
        when(productRepository.findByIdAndBranchId("prod-2", branchId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        // Stubbing for getOrderCookingStatus inside broadcast
        when(orderRepository.findById("order-active")).thenReturn(Optional.of(activeOrder));
        List<OrderItem> itemsList = new ArrayList<>();
        itemsList.add(existingItem);
        itemsList.add(newItem);
        when(orderItemRepository.findByOrderId("order-active")).thenReturn(itemsList);
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(Product.builder().productName("Phở Bò").build()));
        when(productRepository.findById("prod-2")).thenReturn(Optional.of(product));

        CreateOrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals("order-active", response.getOrderId());
        assertEquals(BigDecimal.valueOf(125000), activeOrder.getSubtotal());
        verify(customerSseService, times(1)).broadcastOrderUpdate(eq("order-active"), any(OrderCookingStatusResponse.class));
    }

    @Test
    public void testGetOrderCookingStatus_NotFound_ThrowsException() {
        when(orderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> {
            orderService.getOrderCookingStatus("invalid-id");
        });
    }

    @Test
    public void testGetActiveOrderCookingStatusByTable_TableNotOccupied_ThrowsException() {
        String tableId = "table-1";
        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .status(RestaurantTableStatus.AVAILABLE)
                .build();

        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));

        assertThrows(AppException.class, () -> {
            orderService.getActiveOrderCookingStatusByTable(tableId);
        });
    }

    @Test
    public void testCreateOrder_NullNote_Success() {
        String branchId = "branch-1";
        String tableId = "table-1";

        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .status(RestaurantTableStatus.AVAILABLE)
                .build();

        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .branchId(branchId)
                .tableId(tableId)
                .orderType(OrderType.DINE_IN)
                .note(null) // Null note should be valid
                .items(Collections.singletonList(
                        CreateOrderItemRequestDto.builder()
                                .productId("prod-1")
                                .quantity(1)
                                .build()
                ))
                .build();

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .tableId(tableId)
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Product product = Product.builder()
                .id("prod-1")
                .price(BigDecimal.valueOf(50000))
                .build();

        when(organizationBranchRepository.existsById(branchId)).thenReturn(true);
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(tableId, branchId)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.findByIdAndBranchId("prod-1", branchId)).thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId("order-1")).thenReturn(Collections.emptyList());

        CreateOrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals("order-1", response.getOrderId());
    }
}
