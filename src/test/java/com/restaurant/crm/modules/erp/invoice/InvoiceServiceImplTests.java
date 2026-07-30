package com.restaurant.crm.modules.erp.invoice;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;
import com.restaurant.crm.modules.erp.invoice.dto.request.CheckoutRequestDto;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceResponse;
import com.restaurant.crm.modules.erp.invoice.entity.Invoice;
import com.restaurant.crm.modules.erp.invoice.enums.InvoiceStatus;
import com.restaurant.crm.modules.erp.invoice.enums.PaymentMethod;
import com.restaurant.crm.modules.erp.invoice.mapper.InvoiceMapper;
import com.restaurant.crm.modules.erp.invoice.repository.InvoiceRepository;
import com.restaurant.crm.modules.erp.invoice.service.impl.InvoiceServiceImpl;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceImplTests {

    @Mock
    InvoiceRepository invoiceRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    OrderItemModifierRepository orderItemModifierRepository;
    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ComboRepository comboRepository;
    @Mock
    ModifierOptionRepository modifierOptionRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    PointWalletService pointWalletService;
    @Mock
    InvoiceMapper invoiceMapper;

    @InjectMocks
    InvoiceServiceImpl invoiceService;

    @Test
    public void testCheckout_Success() {
        String orderId = "order-1";
        String branchId = "branch-1";
        String customerPhone = "0987654321";
        String customerId = "cust-1";
        String tableId = "table-1";

        Order order = Order.builder()
                .id(orderId)
                .branchId(branchId)
                .orderCode("ORD-123")
                .status(OrderStatus.PENDING)
                .customerPhone(customerPhone)
                .tableId(tableId)
                .subtotal(BigDecimal.valueOf(50000))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(50000))
                .build();

        CheckoutRequestDto request = CheckoutRequestDto.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CASH)
                .note("Checkout note")
                .build();

        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .status(RestaurantTableStatus.OCCUPIED)
                .build();

        Customer customer = Customer.builder()
                .id(customerId)
                .phone(customerPhone)
                .build();

        Invoice invoice = Invoice.builder()
                .id("invoice-1")
                .order(order)
                .invoiceCode("INV-ABC")
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(PaymentMethod.CASH)
                .status(InvoiceStatus.PAID)
                .build();

        InvoiceResponse mappedResponse = InvoiceResponse.builder()
                .id("invoice-1")
                .orderId(orderId)
                .invoiceCode("INV-ABC")
                .status(InvoiceStatus.PAID)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getBranchId).thenReturn(branchId);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(customerRepository.findByPhone(customerPhone)).thenReturn(Optional.of(customer));
            when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(invoiceMapper.toInvoiceResponse(any(Invoice.class))).thenReturn(mappedResponse);
            when(orderItemRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

            InvoiceResponse response = invoiceService.checkout(request);

            assertNotNull(response);
            assertEquals("INV-ABC", response.getInvoiceCode());
            assertEquals(OrderStatus.PAID, order.getStatus());
            assertEquals(RestaurantTableStatus.AVAILABLE, table.getStatus());

            // Points calculation: 50,000 / 10,000 = 5 points
            verify(pointWalletService, times(1)).earnPoints(customerId, branchId, 5, orderId);
            verify(invoiceRepository, times(1)).save(any(Invoice.class));
            verify(restaurantTableRepository, times(1)).save(table);
        }
    }

    @Test
    public void testCheckout_OrderAlreadyPaid_ThrowsException() {
        String orderId = "order-1";
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatus.PAID)
                .build();

        CheckoutRequestDto request = CheckoutRequestDto.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        AppException ex = assertThrows(AppException.class, () -> {
            invoiceService.checkout(request);
        });
        assertEquals(ErrorCode.ORDER_ALREADY_PAID, ex.getErrorCode());
    }

    @Test
    public void testCheckout_ContextMismatch_ThrowsException() {
        String orderId = "order-1";
        String branchId = "branch-1";
        Order order = Order.builder()
                .id(orderId)
                .branchId(branchId)
                .status(OrderStatus.PENDING)
                .build();

        CheckoutRequestDto request = CheckoutRequestDto.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getBranchId).thenReturn("branch-2"); // different branch

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class, () -> {
                invoiceService.checkout(request);
            });
            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    public void testCheckout_OwnerSuccess() {
        String orderId = "order-1";
        String branchId = "branch-1";
        String orgId = "org-1";

        Order order = Order.builder()
                .id(orderId)
                .branchId(branchId)
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.valueOf(20000))
                .totalAmount(BigDecimal.valueOf(20000))
                .build();

        CheckoutRequestDto request = CheckoutRequestDto.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.BANKING)
                .build();

        Organization org = Organization.builder().id(orgId).build();
        OrganizationBranch branch = OrganizationBranch.builder()
                .id(branchId)
                .organization(org)
                .build();

        Invoice invoice = Invoice.builder()
                .id("invoice-1")
                .order(order)
                .invoiceCode("INV-XYZ")
                .paymentMethod(PaymentMethod.BANKING)
                .status(InvoiceStatus.PAID)
                .build();

        InvoiceResponse mappedResponse = InvoiceResponse.builder()
                .invoiceCode("INV-XYZ")
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getBranchId).thenReturn(null);
            mockedAuth.when(AuthUtils::getOrganizationId).thenReturn(orgId);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(organizationBranchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(invoiceMapper.toInvoiceResponse(any(Invoice.class))).thenReturn(mappedResponse);
            when(orderItemRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

            InvoiceResponse response = invoiceService.checkout(request);

            assertNotNull(response);
            assertEquals("INV-XYZ", response.getInvoiceCode());
            assertEquals(OrderStatus.PAID, order.getStatus());
        }
    }

    @Test
    public void testCheckout_SystemAdmin_ThrowsException() {
        String orderId = "order-1";
        String branchId = "branch-1";
        Order order = Order.builder()
                .id(orderId)
                .branchId(branchId)
                .status(OrderStatus.PENDING)
                .build();

        CheckoutRequestDto request = CheckoutRequestDto.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            // System Admin has no branch or organization context
            mockedAuth.when(AuthUtils::getBranchId).thenReturn(null);
            mockedAuth.when(AuthUtils::getOrganizationId).thenReturn(null);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class, () -> {
                invoiceService.checkout(request);
            });
            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    public void testGetInvoiceDetails_Success() {
        String invoiceId = "invoice-1";
        String branchId = "branch-1";

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .build();

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .order(order)
                .invoiceCode("INV-111")
                .build();

        InvoiceResponse mappedResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .invoiceCode("INV-111")
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getBranchId).thenReturn(branchId);

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(mappedResponse);
            when(orderItemRepository.findByOrderId(order.getId())).thenReturn(Collections.emptyList());

            InvoiceResponse response = invoiceService.getInvoiceDetails(invoiceId);

            assertNotNull(response);
            assertEquals("INV-111", response.getInvoiceCode());
        }
    }

    @Test
    public void testGetInvoiceDetails_ContextMismatch_ThrowsException() {
        String invoiceId = "invoice-1";
        String branchId = "branch-1";

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .build();

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .order(order)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getBranchId).thenReturn("branch-2"); // different branch

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            AppException ex = assertThrows(AppException.class, () -> {
                invoiceService.getInvoiceDetails(invoiceId);
            });
            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, ex.getErrorCode());
        }
    }

    @Test
    public void testGetActiveOrderBillDetails_Success() {
        String orderId = "order-1";
        Order order = Order.builder()
                .id(orderId)
                .orderCode("ORD-111")
                .subtotal(BigDecimal.valueOf(100000))
                .totalAmount(BigDecimal.valueOf(100000))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(Collections.emptyList());

        InvoiceResponse response = invoiceService.getActiveOrderBillDetails(orderId);

        assertNotNull(response);
        assertEquals("ORD-111", response.getOrderCode());
        assertEquals(BigDecimal.valueOf(100000), response.getTotalAmount());
    }
}
