package com.restaurant.crm.modules.erp.invoice.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;
import com.restaurant.crm.modules.erp.invoice.constants.InvoiceConstants;
import com.restaurant.crm.modules.erp.invoice.dto.request.CheckoutRequestDto;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceItemModifierResponse;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceItemResponse;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceResponse;
import com.restaurant.crm.modules.erp.invoice.entity.Invoice;
import com.restaurant.crm.modules.erp.invoice.enums.InvoiceStatus;
import com.restaurant.crm.modules.erp.invoice.mapper.InvoiceMapper;
import com.restaurant.crm.modules.erp.invoice.repository.InvoiceRepository;
import com.restaurant.crm.modules.erp.invoice.service.interfaces.InvoiceService;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceServiceImpl implements InvoiceService {

    InvoiceRepository invoiceRepository;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    OrganizationBranchRepository organizationBranchRepository;
    RestaurantTableRepository restaurantTableRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    CustomerRepository customerRepository;
    PointWalletService pointWalletService;
    InvoiceMapper invoiceMapper;
    com.restaurant.crm.modules.crm.loyaltyvoucher.repository.VoucherRepository voucherRepository;
    com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository customerVoucherRepository;

    @Override
    @Transactional
    public InvoiceResponse checkout(CheckoutRequestDto request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        validateContext(order.getBranchId());

        // Apply voucher code if provided
        if (StringUtils.hasText(request.getVoucherCode())) {
            com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher voucher = voucherRepository.findByBranchIdAndVoucherCodeIgnoreCaseAndIsActive(order.getBranchId(), request.getVoucherCode().trim(), (short) 1)
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            if (voucher.getStartAt() != null && Instant.now().isBefore(voucher.getStartAt())) {
                throw new AppException(ErrorCode.VOUCHER_NOT_STARTED_YET);
            }
            if (voucher.getEndAt() != null && Instant.now().isAfter(voucher.getEndAt())) {
                throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
            }
            if (voucher.getExpiredAt() != null && Instant.now().isAfter(voucher.getExpiredAt())) {
                throw new AppException(ErrorCode.CUSTOMER_VOUCHER_EXPIRED);
            }

            if (order.getSubtotal().compareTo(voucher.getMinBillAmount()) < 0) {
                throw new AppException(ErrorCode.CUSTOMER_VOUCHER_MIN_BILL_NOT_MET);
            }

            if (voucher.getUsageLimit() != null) {
                long usedCount = customerVoucherRepository.countByVoucherIdAndStatus(voucher.getId(), com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus.USED);
                if (usedCount >= voucher.getUsageLimit()) {
                    throw new AppException(ErrorCode.VOUCHER_LIMIT_EXCEEDED);
                }
            }

            // Release any existing voucher first
            customerVoucherRepository.findByOrderId(order.getId()).ifPresent(existingCv -> {
                if (existingCv.getVoucher().getVoucherCode() != null) {
                    customerVoucherRepository.delete(existingCv);
                } else {
                    existingCv.setStatus(com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus.AVAILABLE);
                    existingCv.setUsedAt(null);
                    existingCv.setOrderId(null);
                    customerVoucherRepository.save(existingCv);
                }
            });

            // Calculate discount
            BigDecimal discountAmount = order.getSubtotal().multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            order.setDiscountAmount(discountAmount);
            order.setTotalAmount(order.getSubtotal().subtract(discountAmount));
            orderRepository.save(order);

            // Save CustomerVoucher record
            com.restaurant.crm.modules.crm.customeraccount.entity.Customer customer = null;
            if (StringUtils.hasText(order.getCustomerPhone())) {
                customer = customerRepository.findByPhone(order.getCustomerPhone()).orElse(null);
            }

            String voucherSn = "VCODE" + UUID.randomUUID().toString().replace("-", "").substring(0, 11).toUpperCase();

            com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher customerVoucher = com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher.builder()
                    .customer(customer)
                    .branchId(order.getBranchId())
                    .voucher(voucher)
                    .voucherSn(voucherSn)
                    .status(com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus.USED)
                    .usedAt(Instant.now())
                    .orderId(order.getId())
                    .build();
            customerVoucherRepository.save(customerVoucher);
        }

        // Update order status
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Generate and save Invoice
        Invoice invoice = Invoice.builder()
                .order(order)
                .invoiceCode(generateInvoiceCode())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(InvoiceStatus.PAID)
                .paidAt(Instant.now())
                .note(request.getNote())
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Handle loyalty points (1 point per 10,000 VND)
        if (StringUtils.hasText(order.getCustomerPhone())) {
            Customer customer = customerRepository.findByPhone(order.getCustomerPhone()).orElse(null);
            if (customer != null) {
                int points = order.getTotalAmount().divide(BigDecimal.valueOf(10000)).intValue();
                if (points > 0) {
                    OrganizationBranch branch = organizationBranchRepository.findById(order.getBranchId())
                            .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
                    String organizationId = branch.getOrganization() != null ? branch.getOrganization().getId() : null;
                    pointWalletService.earnPoints(customer.getId(), organizationId, points, order.getId());
                }
            }
        }

        // Release restaurant table
        if (StringUtils.hasText(order.getTableId())) {
            RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
            if (table != null) {
                table.setStatus(RestaurantTableStatus.AVAILABLE);
                restaurantTableRepository.save(table);
            }
        }

        // Construct response DTO
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(savedInvoice);
        response.setItems(buildItemResponses(order.getId()));
        return response;
    }

    @Override
    public InvoiceResponse getInvoiceDetails(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        validateContext(invoice.getOrder().getBranchId());

        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoice);
        response.setItems(buildItemResponses(invoice.getOrder().getId()));
        return response;
    }

    @Override
    public InvoiceResponse getActiveOrderBillDetails(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        InvoiceResponse response = InvoiceResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .branchId(order.getBranchId())
                .tableId(order.getTableId())
                .customerPhone(order.getCustomerPhone())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .build();

        response.setItems(buildItemResponses(order.getId()));
        return response;
    }

    private void validateContext(String orderBranchId) {
        String employeeBranchId = AuthUtils.getBranchId();
        if (employeeBranchId != null) {
            if (!orderBranchId.equals(employeeBranchId)) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return;
        }

        String orgId = AuthUtils.getOrganizationId();
        if (orgId != null) {
            OrganizationBranch branch = organizationBranchRepository.findById(orderBranchId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_BRANCH_NOT_FOUND));
            if (branch.getOrganization() == null || !orgId.equals(branch.getOrganization().getId())) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return;
        }

        throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
    }

    private String generateInvoiceCode() {
        return InvoiceConstants.INVOICE_CODE_PREFIX
                + UUID.randomUUID().toString().substring(0, InvoiceConstants.INVOICE_CODE_RANDOM_LENGTH).toUpperCase();
    }

    private List<InvoiceItemResponse> buildItemResponses(String orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<InvoiceItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : orderItems) {
            String itemName = "Unknown Dish";
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    itemName = product.getProductName();
                }
            } else if (item.getComboId() != null) {
                Combo combo = comboRepository.findById(item.getComboId()).orElse(null);
                if (combo != null) {
                    itemName = combo.getComboName();
                }
            }

            List<OrderItemModifier> modifiers = orderItemModifierRepository.findByOrderItemId(item.getId());
            List<InvoiceItemModifierResponse> modifierResponses = new ArrayList<>();
            for (OrderItemModifier modifier : modifiers) {
                String modifierName = "Extra";
                ModifierOption option = modifierOptionRepository.findById(modifier.getModifierOptionId()).orElse(null);
                if (option != null) {
                    modifierName = option.getOptionName();
                }
                modifierResponses.add(InvoiceItemModifierResponse.builder()
                        .modifierOptionId(modifier.getModifierOptionId())
                        .modifierName(modifierName)
                        .additionalPrice(modifier.getAdditionalPrice())
                        .quantity(modifier.getQuantity())
                        .build());
            }

            itemResponses.add(InvoiceItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .comboId(item.getComboId())
                    .itemName(itemName)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .note(item.getNote())
                    .modifiers(modifierResponses)
                    .build());
        }
        return itemResponses;
    }
}
