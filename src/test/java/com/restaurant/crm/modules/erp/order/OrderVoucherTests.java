package com.restaurant.crm.modules.erp.order;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyalty_voucher.entity.Voucher;
import com.restaurant.crm.modules.crm.loyalty_voucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.crm.loyalty_voucher.service.interfaces.CustomerVoucherService;
import com.restaurant.crm.modules.crm.point_wallet.service.interfaces.PointWalletService;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.impl.OrderServiceImpl;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.common.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderVoucherTests {

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
    ComboRepository comboRepository;
    @Mock
    ModifierOptionRepository modifierOptionRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    CustomerSseService customerSseService;
    @Mock
    CustomerVoucherService customerVoucherService;
    @Mock
    CustomerVoucherRepository customerVoucherRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    PointWalletService pointWalletService;
    @Mock
    SseEmitterService sseEmitterService;

    @InjectMocks
    OrderServiceImpl orderService;

    private Order testOrder;
    private Customer testCustomer;
    private CustomerVoucher testCustomerVoucher;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id("c0000000-0000-0000-0000-000000000001")
                .phone("0987654321")
                .build();

        testOrder = Order.builder()
                .id("o0000000-0000-0000-0000-000000000001")
                .branchId("e0000000-0000-0000-0000-000000000001")
                .orderCode("ORD-TEST001")
                .orderType(OrderType.DINE_IN)
                .status(OrderStatus.PENDING)
                .customerPhone("0987654321")
                .subtotal(BigDecimal.valueOf(100000.00))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(100000.00))
                .build();

        Voucher voucher = Voucher.builder()
                .id("v0000000-0000-0000-0000-000000000001")
                .title("Giảm 10%")
                .discountPercent(10)
                .minBillAmount(BigDecimal.ZERO)
                .build();

        testCustomerVoucher = CustomerVoucher.builder()
                .id("cv000000-0000-0000-0000-000000000001")
                .customer(testCustomer)
                .restaurantId("e0000000-0000-0000-0000-000000000001")
                .voucher(voucher)
                .voucherSn("VSN-TEST001")
                .build();
    }

    @Test
    void testCreateOrder_WithCustomerPhone_RegistersCustomerAndWallet() {
        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .branchId("e0000000-0000-0000-0000-000000000001")
                .orderType(OrderType.TAKEAWAY)
                .customerPhone("0987654321")
                .items(new ArrayList<>())
                .build();

        when(organizationBranchRepository.existsById(any())).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(customerRepository.findByPhone("0987654321")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenReturn(testCustomer);
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(testOrder.getId())).thenReturn(new ArrayList<>());

        CreateOrderResponse response = orderService.create(request);

        assertNotNull(response);
        verify(customerRepository, times(1)).save(any());
        verify(pointWalletService, times(1)).initializeWallet(testCustomer.getId(), "e0000000-0000-0000-0000-000000000001");
    }

    @Test
    void testGetApplicableVouchers_Success() {
        when(orderRepository.findById("o0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(testOrder));
        when(customerRepository.findByPhone("0987654321")).thenReturn(Optional.of(testCustomer));
        
        CustomerVoucherApplicableResponse applicableResponse = CustomerVoucherApplicableResponse.builder()
                .customerVoucherId("cv000000-0000-0000-0000-000000000001")
                .voucherSn("VSN-TEST001")
                .title("Giảm 10%")
                .discountPercent(10)
                .isApplicable(true)
                .build();
        
        when(customerVoucherService.getApplicableVouchers(testCustomer.getId(), testOrder.getBranchId(), testOrder.getSubtotal()))
                .thenReturn(List.of(applicableResponse));

        List<CustomerVoucherApplicableResponse> results = orderService.getApplicableVouchers("o0000000-0000-0000-0000-000000000001");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsApplicable());
        assertEquals("Giảm 10%", results.get(0).getTitle());
    }

    @Test
    void testApplyVoucher_Success() {
        when(orderRepository.findById("o0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(testOrder));
        
        CustomerVoucherResponse cvResponse = CustomerVoucherResponse.builder()
                .id("cv000000-0000-0000-0000-000000000001")
                .voucherSn("VSN-TEST001")
                .voucher(VoucherResponse.builder()
                        .discountPercent(10)
                        .minBillAmount(BigDecimal.ZERO)
                        .build())
                .build();

        when(customerVoucherService.useVoucher("cv000000-0000-0000-0000-000000000001", "o0000000-0000-0000-0000-000000000001", testOrder.getSubtotal()))
                .thenReturn(cvResponse);

        orderService.applyVoucher("o0000000-0000-0000-0000-000000000001", "cv000000-0000-0000-0000-000000000001");

        verify(customerVoucherService, times(1)).releaseVoucher("o0000000-0000-0000-0000-000000000001");
        verify(customerVoucherService, times(1)).useVoucher("cv000000-0000-0000-0000-000000000001", "o0000000-0000-0000-0000-000000000001", BigDecimal.valueOf(100000.00));
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(testOrder.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(90000.00).compareTo(testOrder.getTotalAmount()));
        verify(customerSseService, times(1)).broadcastOrderUpdate(eq("o0000000-0000-0000-0000-000000000001"), any());
    }

    @Test
    void testRemoveVoucher_Success() {
        testOrder.setDiscountAmount(BigDecimal.valueOf(10000.00));
        testOrder.setTotalAmount(BigDecimal.valueOf(90000.00));

        when(orderRepository.findById("o0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(testOrder));

        orderService.removeVoucher("o0000000-0000-0000-0000-000000000001");

        verify(customerVoucherService, times(1)).releaseVoucher("o0000000-0000-0000-0000-000000000001");
        assertEquals(0, BigDecimal.ZERO.compareTo(testOrder.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(100000.00).compareTo(testOrder.getTotalAmount()));
        verify(customerSseService, times(1)).broadcastOrderUpdate(eq("o0000000-0000-0000-0000-000000000001"), any());
    }

    @Test
    void testApplyVoucher_Expired_ThrowsException() {
        when(orderRepository.findById("o0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(testOrder));
        doThrow(new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED))
                .when(customerVoucherService).useVoucher(eq("cv000000-0000-0000-0000-000000000001"), eq("o0000000-0000-0000-0000-000000000001"), any());

        AppException ex = assertThrows(AppException.class, () -> {
            orderService.applyVoucher("o0000000-0000-0000-0000-000000000001", "cv000000-0000-0000-0000-000000000001");
        });

        assertEquals(ErrorCode.CUSTOMER_VOUCHER_EXPIRED, ex.getErrorCode());
    }

    @Test
    void testAddItemsToOrder_WithAppliedVoucher_RecalculatesDiscount() {
        CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                .branchId("e0000000-0000-0000-0000-000000000001")
                .orderType(OrderType.DINE_IN)
                .tableId("t0000000-0000-0000-0000-000000000001")
                .items(List.of(
                        CreateOrderItemRequestDto.builder()
                                .productId("p0000000-0000-0000-0000-000000000001")
                                .quantity(1)
                                .build()
                ))
                .build();

        RestaurantTable table = RestaurantTable.builder()
                .id("t0000000-0000-0000-0000-000000000001")
                .status(RestaurantTableStatus.OCCUPIED)
                .build();

        Product product = Product.builder()
                .id("p0000000-0000-0000-0000-000000000001")
                .price(BigDecimal.valueOf(50000.00))
                .build();

        OrderItem savedItem = OrderItem.builder()
                .id("oi000000-0000-0000-0000-000000000001")
                .subtotal(BigDecimal.valueOf(50000.00))
                .build();

        when(organizationBranchRepository.existsById("e0000000-0000-0000-0000-000000000001")).thenReturn(true);
        when(restaurantTableRepository.findById("t0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(table));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(eq("t0000000-0000-0000-0000-000000000001"), any())).thenReturn(true);
        when(orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc("t0000000-0000-0000-0000-000000000001", OrderStatus.PENDING))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.findById("o0000000-0000-0000-0000-000000000001")).thenReturn(Optional.of(testOrder));
        when(productRepository.findByIdAndBranchId("p0000000-0000-0000-0000-000000000001", "e0000000-0000-0000-0000-000000000001"))
                .thenReturn(Optional.of(product));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedItem);
        when(customerVoucherRepository.findByOrderId("o0000000-0000-0000-0000-000000000001"))
                .thenReturn(Optional.of(testCustomerVoucher)); // 10% voucher
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId("o0000000-0000-0000-0000-000000000001")).thenReturn(new ArrayList<>());

        CreateOrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals("o0000000-0000-0000-0000-000000000001", response.getOrderId());
        
        // original subtotal (100k) + new item (50k) = 150k
        assertEquals(0, BigDecimal.valueOf(150000.00).compareTo(testOrder.getSubtotal()));
        // 10% of 150k = 15k
        assertEquals(0, BigDecimal.valueOf(15000.00).compareTo(testOrder.getDiscountAmount()));
        // 150k - 15k = 135k
        assertEquals(0, BigDecimal.valueOf(135000.00).compareTo(testOrder.getTotalAmount()));
    }
}
